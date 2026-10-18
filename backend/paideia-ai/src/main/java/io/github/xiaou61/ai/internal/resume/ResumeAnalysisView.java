package io.github.xiaou61.ai.internal.resume;

/**
 * 分析结果里的一条要求，渲染给提案那一步看（WORK-018）。
 *
 * <p>它与 {@code ResumeAnalysis.Requirement} 字段相同，但仍然单独一个类型：包根的 record 是
 * 端口契约的一部分，改它会影响端口；这里的渲染视图可以随提示词需要自由调整（比如以后加
 * "这条要求是否已被覆盖"的标记），不必惊动契约。
 */
public record ResumeAnalysisView(String term, String quote) {
}
