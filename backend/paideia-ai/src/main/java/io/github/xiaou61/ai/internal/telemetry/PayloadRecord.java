package io.github.xiaou61.ai.internal.telemetry;

import java.time.Instant;

/**
 * 一轮 LLM 交互的完整输入与输出（WORK-030 阶段 B）。
 *
 * <p>与 {@link RunRecord} 同一纪律：**自包含**。它跨线程交给写队列，那时原始对象可能已经被改过。
 *
 * @param traceId       所属轮次的 trace id
 * @param seq           对应 `ai_run_spans.seq`（第几轮）
 * @param requestBody   实际发给上游的 messages / tools / 采样参数（JSON）；超上限时为 null
 * @param responseBody  模型返回的正文与工具调用（JSON）；超上限时为 null
 * @param requestBytes  原始字节数（即使正文被占位，也留得下"原本多大"）
 * @param responseBytes 同上
 * @param truncated     内容超上限被占位替代
 * @param createdAt     写入时刻（清理按它切片）
 */
public record PayloadRecord(
        String traceId,
        int seq,
        String requestBody,
        String responseBody,
        int requestBytes,
        int responseBytes,
        boolean truncated,
        Instant createdAt) {
}
