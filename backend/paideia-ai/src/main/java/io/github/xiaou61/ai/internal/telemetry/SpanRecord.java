package io.github.xiaou61.ai.internal.telemetry;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 一步的不可变快照（WORK-022）。
 *
 * <p>`seq` 是**轮次内的序号**（从 1 开始），详情页按它排序。`parentSeq` 为空表示顶层；
 * 有值时表示"这一轮工具执行里的子调用"——一期不产生嵌套，但结构留着，将来工具内部
 * 再调模型时不用改表。
 */
public record SpanRecord(
        String traceId,
        Integer parentSeq,
        int seq,
        String kind,
        String name,
        String status,
        String errorType,
        Instant startedAt,
        Integer durationMs,
        Integer ttftMs,
        int inputTokens,
        int outputTokens,
        int cacheReadTokens,
        Map<String, Object> attrs) {

    public SpanRecord {
        attrs = attrs == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(attrs));
    }
}
