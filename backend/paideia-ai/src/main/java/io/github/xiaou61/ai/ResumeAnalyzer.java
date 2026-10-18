package io.github.xiaou61.ai;

/**
 * JD 分析端口（WORK-018）：主简历 + JD → 显式要求清单。
 *
 * <p>不挂任何工具、非流式、只回 JSON：与 {@link EntrySummarizer} 同一套最小面。
 */
public interface ResumeAnalyzer {

    /**
     * @param resume 主简历（给模型看"我有什么"，避免它提出显然不可能的建议）
     * @param jdText JD 原文（不可信内容，进提示词前已由调用方按数据段包裹）
     * @param access 本次调用用的凭据
     */
    Analyzed analyze(ResumeData resume, String jdText, ModelAccess access);

    /**
     * @param analysis 显式要求清单
     * @param usage    本次调用用量；调用方据此结算积分
     */
    record Analyzed(ResumeAnalysis analysis, TokenUsage usage) {
    }
}
