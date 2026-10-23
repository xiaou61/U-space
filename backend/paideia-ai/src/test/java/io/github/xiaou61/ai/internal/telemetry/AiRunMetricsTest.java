package io.github.xiaou61.ai.internal.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 概览与详情的纯计算（WORK-036）。
 *
 * <p>这里只测能从已有数据推出来的那几项：缓存节省比例与耗时构成。
 * 需要数据库的部分（分桶聚合、对账）在集成用例里。
 */
class AiRunMetricsTest {

    @Test
    void cacheSavedRatioIsZeroWhenEverythingIsZero() {
        // NaN 会渲染成 "NaN%"，比没有数字更糟
        assertThat(AiRunAdminService.cacheSavedRatio(0, 0)).isZero();
    }

    @Test
    void cacheSavedRatioCountsBothSidesInTheDenominator() {
        // 命中 3000、未命中 1000 → 本该送进模型的 4000 里有 3000 是省下来的
        assertThat(AiRunAdminService.cacheSavedRatio(1000, 3000)).isEqualTo(0.75);
    }

    @Test
    void breakdownSubtractsEachSegment() {
        List<AiRunAdminService.SpanView> spans = List.of(
                span("llm", 500), span("llm", 500), span("tool", 800));

        AiRunAdminService.Breakdown b = AiRunAdminService.breakdown(100, 2000, spans);

        assertThat(b.queueMs()).isEqualTo(100);
        assertThat(b.llmMs()).isEqualTo(1000);
        assertThat(b.toolMs()).isEqualTo(800);
        assertThat(b.otherMs()).isEqualTo(100);
        assertThat(b.queueMs() + b.llmMs() + b.toolMs() + b.otherMs()).isEqualTo(b.totalMs());
    }

    @Test
    void breakdownClampsOtherToZeroWhenSegmentsOverrun() {
        // 并发工具调用会重叠，加起来超过总耗时是真实会发生的：夹到 0 而不是显示 "-300ms"
        List<AiRunAdminService.SpanView> spans = List.of(span("tool", 1500), span("tool", 1500));

        AiRunAdminService.Breakdown b = AiRunAdminService.breakdown(0, 1000, spans);

        assertThat(b.otherMs()).isZero();
    }

    @Test
    void breakdownIgnoresSpansWithoutDuration() {
        List<AiRunAdminService.SpanView> spans = List.of(span("llm", null), span("llm", 300));

        assertThat(AiRunAdminService.breakdown(null, 400, spans).llmMs()).isEqualTo(300);
    }

    private static AiRunAdminService.SpanView span(String kind, Integer durationMs) {
        return new AiRunAdminService.SpanView(1, null, kind, kind, "ok", null,
                java.time.Instant.EPOCH, durationMs, null, 0, 0, Map.of());
    }
}
