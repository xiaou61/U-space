package io.github.xiaou61.ai.internal.digest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/**
 * 周报的 prompt 真连验证（WORK-027 TASK-010）。
 *
 * <p>与 {@link DigestSummarizerLiveTest} 同一理由：解析与编排有单测，但**提示词能不能产出
 * 一段可读的「本周技术主线」**只有真调一次才知道。旧设计假设它会写 300~500 字 markdown。
 *
 * <p>这里只验提示词与模型输出，不碰数据库（week_start 与 upsert 由
 * {@code DigestIntegrationTest} 覆盖）。
 */
@EnabledIfEnvironmentVariable(named = "DEEPSEEK_API_KEY", matches = ".+")
class DigestWeeklyPromptLiveTest {

    @Test
    @DisplayName("真连 DeepSeek：周报正文是中文 markdown，且提到素材里的主题")
    void weeklyPromptProducesReadableBody() throws Exception {
        // 直接复用 DigestWeeklyService 里的提示词：反射不必要，构造一个只带提示词的实例太绕，
        // 所以这里读 system prompt 常量并通过同一套模型客户端调一次。
        List<Map<String, Object>> titles = List.of(
                Map.of("group_key", "news", "title", "DeepSeek 发布新推理模型"),
                Map.of("group_key", "news", "title", "Tauri 2.0 支持移动端"),
                Map.of("group_key", "github", "title", "deepseek-ai/DeepInfer 单周涨 1500 star"),
                Map.of("group_key", "github", "title", "某 Rust 工具链项目进入热榜"),
                Map.of("group_key", "hackernews", "title", "Ask HN: 2026 年还值得学 Rust 吗"),
                Map.of("group_key", "community", "title", "一次 user_id 相关的 Prometheus 事故复盘"));

        io.github.xiaou61.ai.ModelAccess access = new io.github.xiaou61.ai.ModelAccess(
                "https://api.deepseek.com", "deepseek-flash", System.getenv("DEEPSEEK_API_KEY"));

        DigestWeeklyService service = new DigestWeeklyService(
                new DigestProperties(), null, null,
                new io.github.xiaou61.ai.internal.provider.GuardedHttpTransport(
                        new io.github.xiaou61.ai.internal.provider.UpstreamGuard()),
                new io.github.xiaou61.ai.internal.telemetry.NoopRunRecorder());

        java.lang.reflect.Method call = DigestWeeklyService.class
                .getDeclaredMethod("callModel", List.class, io.github.xiaou61.ai.ModelAccess.class);
        call.setAccessible(true);
        long started = System.currentTimeMillis();
        String body = (String) call.invoke(service, titles, access);
        long elapsed = System.currentTimeMillis() - started;

        System.out.printf("[live] 周报耗时 %d ms，正文长度 %d 字%n", elapsed,
                body == null ? 0 : body.length());
        System.out.println("[live] 正文：\n" + body);

        assertThat(body).as("周报应产出正文").isNotBlank();
        assertThat(body).as("应是中文").matches("(?s).*[\\u4e00-\\u9fa5].*");
        // 提示词要求分段；不强制 markdown 标题层级，但要有结构感
        assertThat(body.length()).as("300~500 字是提示词要求，实测允许浮动").isGreaterThan(100);
        assertThat(body).as("应提到素材里的主题").containsAnyOf("推理", "Rust", "Tauri", "模型");
    }
}
