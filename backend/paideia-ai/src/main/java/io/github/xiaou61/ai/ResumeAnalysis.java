package io.github.xiaou61.ai;

import java.util.List;

/**
 * JD 分析结果（WORK-018）。
 *
 * <p>它只承载**模型能可靠给出的那部分**：这个岗位显式要求了哪些词、每条要求出自 JD 的哪一句。
 * **分桶不在这里**——那是代码拿这些要求去主简历里查出来的（{@link io.github.xiaou61.ai.internal.resume.ResumeKeywordClassifier}）。
 * 分开的理由：模型自评"候选人会不会"是幻觉温床，而字符串查找可测、可复现、不花钱。
 *
 * @param requirements JD 的显式要求，按重要性降序（模型给的顺序即界面顺序）
 * @param summary      一句话说明这个岗位最看重什么（给界面做标题用，不参与任何判定）
 */
public record ResumeAnalysis(List<Requirement> requirements, String summary) {

    /**
     * 一条 JD 要求。
     *
     * @param term  要求本身，尽量是**技能/能力名词**而不是整句（分桶按它查找）
     * @param quote JD 里的原句，用作"这条要求确实来自 JD"的凭据
     */
    public record Requirement(String term, String quote) {
    }
}
