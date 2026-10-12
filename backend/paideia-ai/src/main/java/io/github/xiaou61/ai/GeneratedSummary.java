package io.github.xiaou61.ai;

/**
 * 一次生成的产物：卡片 + 用量 + 是否截断。
 *
 * <p>端口只负责"调一次模型并返回**已校验**的卡片"；缓存、可见性、限流都在调用方
 * （{@code internal.summary.SummaryService}）——这样换模型实现只影响一个包。
 */
public record GeneratedSummary(SummaryCard card, TokenUsage usage, boolean truncated) {
}
