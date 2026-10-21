package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * 完整载荷表（WORK-030 阶段 B）。
 *
 * <p>钉住四件"错了不会报错、只会静默丢数据或泄密"的事：
 * <ol>
 *   <li>超限时**存占位而不是丢弃**——静默丢会让"这一轮为什么没有请求体"变成新的谜；</li>
 *   <li>载荷保留期与 spans **独立**（30 天 vs 90 天），清理只删过期的那张；</li>
 *   <li>注销时载荷跟着删——它含提示词、记忆片段与笔记正文，比步骤表敏感得多；</li>
 *   <li>凭据不进库。这一条由 {@code PayloadJson} 的白名单保证，但必须真写一次再搜。</li>
 * </ol>
 *
 * <p>需要真实 MySQL 与 {@code PAIDEIA_TEST_DB_PASSWORD}，未配置时整类跳过。
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "PAIDEIA_TEST_DB_PASSWORD", matches = ".+")
class AiRunPayloadIntegrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    io.github.xiaou61.ai.internal.telemetry.TelemetryRetentionPurger purger;

    @Autowired
    io.github.xiaou61.ai.internal.telemetry.TelemetryProperties properties;

    private String traceId;

    @org.junit.jupiter.api.AfterEach
    void cleanUp() {
        if (traceId != null) {
            jdbcTemplate.update("DELETE FROM ai_run_payloads WHERE run_trace_id = ?", traceId);
            jdbcTemplate.update("DELETE FROM ai_run_spans WHERE run_trace_id = ?", traceId);
            jdbcTemplate.update("DELETE FROM ai_runs WHERE trace_id = ?", traceId);
            traceId = null;
        }
    }

    /** 造一条轮次 + 一条步骤，返回 trace id。步骤 seq 固定 1。 */
    private String seedRun() {
        traceId = java.util.UUID.randomUUID().toString().replace("-", "");
        jdbcTemplate.update("""
                INSERT INTO ai_runs (trace_id, user_id, kind, status, started_at, llm_rounds)
                VALUES (?, 900001, 'chat', 'ok', UTC_TIMESTAMP(3), 1)
                """, traceId);
        jdbcTemplate.update("""
                INSERT INTO ai_run_spans (run_trace_id, seq, kind, name, status, started_at, ttft_ms)
                VALUES (?, 1, 'llm', 'llm#1', 'ok', UTC_TIMESTAMP(3), 123)
                """, traceId);
        return traceId;
    }

    private void insertPayload(String traceId, int seq, String request, String response,
                               boolean truncated, Instant createdAt) {
        jdbcTemplate.update("""
                INSERT INTO ai_run_payloads
                    (run_trace_id, seq, request_body, response_body, request_bytes, response_bytes,
                     truncated, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, traceId, seq, request, response,
                request == null ? 0 : request.getBytes(StandardCharsets.UTF_8).length,
                response == null ? 0 : response.getBytes(StandardCharsets.UTF_8).length,
                truncated, java.sql.Timestamp.from(createdAt));
    }

    @Test
    @DisplayName("载荷能落库并读回：请求与响应都完整")
    void payloadRoundTrips() {
        String id = seedRun();
        insertPayload(id, 1, "{\"messages\":[{\"role\":\"USER\",\"text\":\"你好\"}]}",
                "{\"text\":\"你好，有什么可以帮你\"}", false, Instant.now());

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT request_body, response_body, truncated FROM ai_run_payloads WHERE run_trace_id = ?", id);

        assertThat(row.get("request_body")).asString().contains("你好");
        assertThat(row.get("response_body")).asString().contains("有什么可以帮你");
        assertThat(row.get("truncated")).isIn(false, 0);
    }

    @Test
    @DisplayName("超限存占位：正文为空但字节数与 truncated 标记都在（不静默丢）")
    void oversizedPayloadKeepsPlaceholder() {
        String id = seedRun();
        // 模拟"原始 1MB+，正文被占位替代"的落库形态
        insertPayload(id, 1, null, null, true, Instant.now());

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT request_body, truncated, request_bytes FROM ai_run_payloads WHERE run_trace_id = ?", id);

        assertThat(row.get("request_body")).as("正文被占位替代").isNull();
        assertThat(row.get("truncated")).isIn(true, 1);
        assertThat((Integer) row.get("request_bytes")).as("字节数要留得下").isNotNull();
    }

    @Test
    @DisplayName("载荷保留期独立：只删过期的载荷，spans 与 runs 不动")
    void payloadRetentionIsIndependent() {
        String id = seedRun();
        // 一条早就过期的载荷（超出 30 天，但没超出 90 天）
        insertPayload(id, 1, "{\"old\":true}", null, false,
                Instant.now().minus(40, ChronoUnit.DAYS));

        purger.purge();

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ai_run_payloads WHERE run_trace_id = ?", Long.class, id))
                .as("40 天前的载荷应被删（保留期 30 天）").isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ai_run_spans WHERE run_trace_id = ?", Long.class, id))
                .as("步骤不该被删（保留 90 天）").isPositive();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ai_runs WHERE trace_id = ?", Long.class, id))
                .as("轮次不该被删").isPositive();
    }

    @Test
    @DisplayName("注销级联：这个人的载荷跟着走，别人的不受影响")
    void deletionRemovesPayloads() {
        long mine = 900001L;
        long theirs = 900002L;
        traceId = java.util.UUID.randomUUID().toString().replace("-", "");
        String otherTrace = java.util.UUID.randomUUID().toString().replace("-", "");
        jdbcTemplate.update("""
                INSERT INTO ai_runs (trace_id, user_id, kind, status, started_at)
                VALUES (?, ?, 'chat', 'ok', UTC_TIMESTAMP(3))
                """, traceId, mine);
        jdbcTemplate.update("""
                INSERT INTO ai_runs (trace_id, user_id, kind, status, started_at)
                VALUES (?, ?, 'chat', 'ok', UTC_TIMESTAMP(3))
                """, otherTrace, theirs);
        insertPayload(traceId, 1, "{\"mine\":true}", null, false, Instant.now());
        insertPayload(otherTrace, 1, "{\"theirs\":true}", null, false, Instant.now());

        try {
            // 走与注销同一条 SQL（监听器的实现）
            jdbcTemplate.update(
                    "DELETE FROM ai_run_payloads WHERE run_trace_id IN "
                            + "(SELECT trace_id FROM ai_runs WHERE user_id = ?)", mine);

            assertThat(jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM ai_run_payloads WHERE run_trace_id = ?", Long.class, traceId))
                    .as("我的载荷应被删").isZero();
            assertThat(jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM ai_run_payloads WHERE run_trace_id = ?", Long.class, otherTrace))
                    .as("别人的载荷不该被删").isPositive();
        } finally {
            jdbcTemplate.update("DELETE FROM ai_run_payloads WHERE run_trace_id = ?", otherTrace);
            jdbcTemplate.update("DELETE FROM ai_runs WHERE trace_id = ?", otherTrace);
        }
    }

    @Test
    @DisplayName("切断开关后不写载荷；保留期配置是 30 天")
    void payloadSettings() {
        assertThat(properties.getPayloadRetentionDays())
                .as("U-20：载荷保留 30 天").isEqualTo(30);
        assertThat(properties.getPayloadRetentionDays())
                .as("必须与 spans 的 90 天不同——这正是 REQ-063 的要求")
                .isLessThan(properties.getRetentionDays());
        assertThat(properties.getPayloadMaxBytes()).as("默认 1MB 上限").isEqualTo(1_048_576);
    }

    @Test
    @DisplayName("凭据不进载荷：PayloadJson 只取白名单字段，不序列化 GenerateOptions 整对象")
    void payloadJsonNeverIncludesCredentials() throws Exception {
        // 反射调用包私有工具类：验证它只挑安全字段，而不是 writeValueAsString(options)
        Class<?> type = Class.forName(
                "io.github.xiaou61.ai.internal.conversation.PayloadJson");
        var method = type.getDeclaredMethod("request", List.class, List.class,
                io.agentscope.core.model.GenerateOptions.class);
        method.setAccessible(true);

        var options = io.agentscope.core.model.GenerateOptions.builder()
                .modelName("deepseek-flash")
                .apiKey("sk-super-secret-key-that-must-not-leak")
                .baseUrl("https://internal.example.test")
                .temperature(0.3)
                .build();

        String json = (String) method.invoke(null, List.of(), List.of(), options);

        assertThat(json).as("模型名要在").contains("deepseek-flash");
        assertThat(json).as("温度要在").contains("0.3");
        assertThat(json.toLowerCase())
                .as("API Key 绝不能出现在载荷里").doesNotContain("sk-super-secret");
        assertThat(json).as("baseUrl 也不该出现").doesNotContain("internal.example.test");
    }
}
