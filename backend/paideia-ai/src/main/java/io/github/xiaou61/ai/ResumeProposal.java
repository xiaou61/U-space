package io.github.xiaou61.ai;

import java.util.List;

/**
 * 一条改写提案（WORK-018）。
 *
 * <p><b>它是"逐项采纳"的数据形态</b>：一处改动 = 一条提案，带原文片段、改后片段、改动类型、
 * 依据与覆盖的关键词。不采用"交一整稿再让用户找差异"的形态，因为差异反推无法可靠回答
 * "这处改动是哪条 JD 要求驱动的"——而那正是用户要说"我接受"还是"我不接受"的依据。
 */
public record ResumeProposal(
        /** 指向主简历里的位置，如 {@code work[0].bullets[2]}；闸门靠它回查原文。 */
        String entryRef,
        /** 改动落在哪个字段：bullet / summary / title / skillGroup / skillItem。 */
        String field,
        /** 原文片段。必须能在主简历对应位置找到，否则服务端丢弃该条。 */
        String originalText,
        /** 改后片段。 */
        String proposedText,
        /** rewrite | reorder | add | remove。 */
        String changeType,
        /** 为什么这么改：引用 JD 原句，或说明"排序调整"。 */
        String rationale,
        /** 这条改动覆盖了哪些 JD 关键词。 */
        List<String> coversKeywords) {
}
