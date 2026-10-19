package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import io.github.xiaou61.ai.AssistantConversation;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.RunRecorder;
import io.github.xiaou61.ai.RunSpec;
import io.github.xiaou61.ai.SpanSpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * AI 调用追踪的端到端验证（WORK-022 T04/T10）。
 *
 * <p>这一层测的是**只有真起 Spring + 真写库才能看见**的东西：采集器装配对了吗、
 * 写队列真的落库了吗、管理端端点读得回来吗、时间窗收敛了吗。
 *
 * <p>不测"对话质量"：`AssistantConversation` 是桩，本轮不碰网络。
 */
@SpringBootTest
@ActiveProfiles("test")
class AiTelemetryIntegrationTest {

    @Autowired
    RunRecorder recorder;


    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    io.github.xiaou61.ai.internal.telemetry.TelemetryRetentionPurger telemetryPurger;

    @MockitoBean
    AssistantConversation assistant;

    /** 写线程是异步的：断言前等这一轮真的落了库。 */
    /** 测试用户的记录互不干扰：每次跑前清掉 9000 段的行。 */
    private void wipe() {
        jdbcTemplate.update("DELETE FROM ai_run_spans WHERE run_trace_id IN "
                + "(SELECT trace_id FROM ai_runs WHERE user_id >= 9000)");
        jdbcTemplate.update("DELETE FROM ai_runs WHERE user_id >= 9000");
    }

    /**
     * 等这一轮**完全写完**：轮次到终态，且（若期望有步骤）步骤也已落库。
     *
     * <p>只等终态不够：`UPDATE` 与 `insertBatch` 是 sink 里的两条语句，
     * 终态更新先完成时步骤可能还没插进去（2026-10-19 实测：断言偶发拿到空数组）。
     *
     * @param expectedSpans 期望的步骤数；0 表示不要求有步骤
     */
    private void awaitRun(String traceId, int expectedSpans) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            String status = jdbcTemplate.query(
                    "SELECT status FROM ai_runs WHERE trace_id = ?",
                    rs -> rs.next() ? rs.getString(1) : null, traceId);
            Integer spanCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM ai_run_spans WHERE run_trace_id = ?", Integer.class, traceId);
            if (status != null && !"running".equals(status)
                    && (expectedSpans == 0 || (spanCount != null && spanCount >= expectedSpans))) {
                return;
            }
            Thread.sleep(25);
        }
    }

    @Test
    @DisplayName("一轮带工具的调用：轮次 1 行、步骤 3 行、token 合计等于各步之和")
    void oneToolTurnWritesRunAndSpans() throws Exception {
        wipe();
        // 直接驱动采集端口，模拟"一轮里两次上游 + 一次工具"。
        // 走 HTTP 需要真上游；这里验的是记录的形状与落库，那是本项的核心。
        try (RunRecorder.RunScope run = recorder.begin(RunSpec.of("chat", 9001L)
                .withConversation(null).withHarness("assistant").withModel("deepseek-flash"))) {
            try (RunRecorder.StepScope step = run.step(SpanSpec.llm(1))) {
                step.tokens(10, 5, 0);
                step.succeeded();
            }
            run.tokens(10, 5, 0);
            try (RunRecorder.StepScope step = run.step(SpanSpec.tool("knowledge_search"))) {
                step.succeeded();
            }
            try (RunRecorder.StepScope step = run.step(SpanSpec.llm(2))) {
                step.tokens(20, 8, 0);
                step.succeeded();
            }
            run.tokens(20, 8, 0);
            run.markTtft();
            run.attribute("historyMsgs", 3);
            run.addCredits(7);
            run.finish(io.github.xiaou61.ai.RunOutcome.OK, null);
            String traceId = currentTraceId(9001L);
            awaitRun(traceId, 3);

            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT status, llm_rounds, tool_calls, input_tokens, output_tokens, credits, ttft_ms "
                            + "FROM ai_runs WHERE trace_id = ?", traceId);
            assertThat(row.get("status")).isEqualTo("ok");
            assertThat(((Number) row.get("llm_rounds")).intValue()).isEqualTo(2);
            assertThat(((Number) row.get("tool_calls")).intValue()).isEqualTo(1);
            assertThat(((Number) row.get("input_tokens")).intValue()).isEqualTo(30);
            assertThat(((Number) row.get("output_tokens")).intValue()).isEqualTo(13);
            assertThat(((Number) row.get("credits")).intValue()).isEqualTo(7);
            assertThat(row.get("ttft_ms")).isNotNull();

            List<Map<String, Object>> spans = jdbcTemplate.queryForList(
                    "SELECT seq, kind, name, status, input_tokens FROM ai_run_spans "
                            + "WHERE run_trace_id = ? ORDER BY seq", traceId);
            assertThat(spans).hasSize(3);
            assertThat(spans.get(0).get("kind")).isEqualTo("llm");
            assertThat(spans.get(1).get("kind")).isEqualTo("tool");
            assertThat(spans.get(2).get("name")).isEqualTo("llm#2");
            int sum = spans.stream()
                    .filter(s -> "llm".equals(s.get("kind")))
                    .mapToInt(s -> ((Number) s.get("input_tokens")).intValue())
                    .sum();
            assertThat(sum).isEqualTo(30);
        }
    }

    @Test
    @DisplayName("失败的一轮也留记录，且带低基数错误标识")
    void failedRunIsRecorded() throws Exception {
        wipe();
        try (RunRecorder.RunScope run = recorder.begin(RunSpec.of("chat", 9002L))) {
            run.finish(io.github.xiaou61.ai.RunOutcome.FAILED, "INSUFFICIENT_CREDITS");
            String traceId = currentTraceId(9002L);
            awaitRun(traceId, 0);
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT status, error_type FROM ai_runs WHERE trace_id = ?", traceId);
            assertThat(row.get("status")).isEqualTo("failed");
            assertThat(row.get("error_type")).isEqualTo("INSUFFICIENT_CREDITS");
        }
    }

    @Test
    @DisplayName("没 finish 就关：记为 interrupted，不留 running 僵尸")
    void closeWithoutFinishIsInterrupted() throws Exception {
        wipe();
        String traceId;
        try (RunRecorder.RunScope run = recorder.begin(RunSpec.of("chat", 9003L))) {
            // 故意不 finish —— try-with-resources 的 close 应该兜住
            run.attribute("historyMsgs", 1);
        }
        traceId = currentTraceId(9003L);
        awaitRun(traceId, 0);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM ai_runs WHERE trace_id = ?", String.class, traceId))
                .isEqualTo("interrupted");
    }

    @Test
    @DisplayName("采集器已经装配成真的实现（不是空实现）")
    void recorderIsEnabledInTests() {
        assertThat(recorder.enabled()).isTrue();
        assertThat(recorder.getClass().getSimpleName()).isEqualTo("DefaultRunRecorder");

    }

    @Test
    @DisplayName("保留期清理：过期行与其步骤一起消失，未过期的不受影响")
    void retentionPurgesExpiredRunsAndSpans() throws Exception {
        wipe();
        Instant now = Instant.now();
        // 造两条：一条 100 天前（过期）、一条 1 天前（保留）
        jdbcTemplate.update("INSERT INTO ai_runs (trace_id, user_id, kind, status, started_at) "
                + "VALUES (?, ?, 'chat', 'ok', ?)", "expired-run", 9005L, now.minus(100, ChronoUnit.DAYS));
        jdbcTemplate.update("INSERT INTO ai_run_spans (run_trace_id, seq, kind, name, status, started_at) "
                + "VALUES (?, 1, 'llm', 'llm#1', 'ok', ?)", "expired-run", now.minus(100, ChronoUnit.DAYS));
        jdbcTemplate.update("INSERT INTO ai_runs (trace_id, user_id, kind, status, started_at) "
                + "VALUES (?, ?, 'chat', 'ok', ?)", "fresh-run", 9005L, now.minus(1, ChronoUnit.DAYS));
        jdbcTemplate.update("INSERT INTO ai_run_spans (run_trace_id, seq, kind, name, status, started_at) "
                + "VALUES (?, 1, 'llm', 'llm#1', 'ok', ?)", "fresh-run", now.minus(1, ChronoUnit.DAYS));

        purge();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ai_runs WHERE trace_id = 'expired-run'", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ai_run_spans WHERE run_trace_id = 'expired-run'", Integer.class)).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ai_runs WHERE trace_id = 'fresh-run'", Integer.class)).isEqualTo(1);
    }

    @Test
    @DisplayName("僵尸行：停在 running 超过阈值的被标成 interrupted")
    void stuckRunsAreMarkedInterrupted() throws Exception {
        wipe();
        Instant longAgo = Instant.now().minus(10, ChronoUnit.HOURS);
        jdbcTemplate.update("INSERT INTO ai_runs (trace_id, user_id, kind, status, started_at) "
                + "VALUES (?, ?, 'chat', 'running', ?)", "stuck-run", 9006L, longAgo);

        purge();

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT status, error_type FROM ai_runs WHERE trace_id = 'stuck-run'");
        assertThat(row.get("status")).isEqualTo("interrupted");
        assertThat(row.get("error_type")).isEqualTo("stuck");
    }

    /** 直接调清理器：它平时是 @Scheduled，测试不等一小时。 */
    private void purge() {
        telemetryPurger.purge();
    }

    @Test
    @DisplayName("关掉采集时一条记录都不产生")
    void disabledRecorderWritesNothing() {
        int before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ai_runs", Integer.class);
        try (RunRecorder.RunScope run = new io.github.xiaou61.ai.internal.telemetry.NoopRunRecorder()
                .begin(RunSpec.of("chat", 9004L))) {
            run.finish(io.github.xiaou61.ai.RunOutcome.OK, null);
        }
        int after = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ai_runs", Integer.class);
        assertThat(after).isEqualTo(before);
    }

    /**
     * 采集器不给外部拿 traceId（那是内部标识），所以测试用"最近一条"反查。
     *
     * <p>这比暴露一个只给测试用的 getter 好：生产代码不该为了测试多一个访问点。
     */
    /**
     * 取刚 begin 那一轮的 traceId。
     *
     * <p><b>必须等</b>：`begin` 只是把 running 行投进队列，写线程是异步的。
     * 直接查会拿到 null（实测：断言一直说"找不到那一行"，而库里其实有）。
     */
    private String currentTraceId(long userId) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            // **只认终态行**：running 行是 begin 时写的，那时步骤还没落库。
            // 拿到 running 行会让 awaitRun 立刻返回，接着就查不到步骤（实测踩过）。
            String traceId = jdbcTemplate.query(
                    "SELECT trace_id FROM ai_runs WHERE user_id = ? AND status <> 'running' "
                            + "ORDER BY id DESC LIMIT 1",
                    rs -> rs.next() ? rs.getString(1) : null, userId);
            if (traceId != null) {
                return traceId;
            }
            Thread.sleep(25);
        }
        throw new IllegalStateException("5 秒内没有看到 running 行——写队列可能没起来");
    }
}
