package io.github.xiaou61.ai;

import java.util.List;

/**
 * 改写提案端口（WORK-018）。
 *
 * <p>它是三次调用里最贵的一次，也是唯一一次"模型直接产出要进定稿的文字"。因此它的输出
 * 必须是最小单元（一条一处改动），且每条都带原文片段——后续事实闸门靠原文片段判断改写
 * 有没有越界。
 */
public interface ResumeProposer {

    /**
     * @param resume   主简历（唯一事实来源）
     * @param jdText   JD 原文
     * @param analysis 上一步的分析结果（模型据此知道哪些要求已被覆盖、哪些是缺口）
     * @param gaps     缺口关键词。**模型会被明确告知：不得在改写里出现这些词**
     * @param access   本次调用用的凭据
     */
    Proposed propose(ResumeData resume,
                     String jdText,
                     ResumeAnalysis analysis,
                     List<String> gaps,
                     ModelAccess access);

    /**
     * @param proposals 通过结构校验的提案（已丢弃位置不存在、原文对不上、含缺口词的条目）
     * @param dropped   被丢弃的条数；界面用它提示"模型给了 N 条，M 条没通过校验"
     * @param usage     本次调用用量；调用方据此结算积分
     */
    record Proposed(List<ResumeProposal> proposals, int dropped, TokenUsage usage) {
    }
}
