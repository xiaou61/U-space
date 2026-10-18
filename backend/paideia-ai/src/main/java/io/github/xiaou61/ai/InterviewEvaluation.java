package io.github.xiaou61.ai;

import java.util.List;

/**
 * 一场面试的评估（WORK-010）。
 *
 * <p>它是**结构化**的，因为它要落库、要能查进度：塞进消息文本等于不可查。
 *
 * <p>字段刻意少：`overall` + 几个维度 + 优点 + 改进点。不做"每道题一个分数"——
 * 那需要先有题库与题目编号，而一期的题是模型按知识库临时出的，题号不稳定。
 *
 * @param overall      总分 1–5
 * @param dimensions   各维度（2–4 个）
 * @param strengths    具体优点（1–4 条）
 * @param improvements 具体可执行的改进建议（1–4 条）
 */
public record InterviewEvaluation(int overall, List<Dimension> dimensions,
                                  List<String> strengths, List<String> improvements) {

    /**
     * 一个维度。
     *
     * @param name    维度名（中文，如"技术准确性"）
     * @param score   1–5
     * @param comment 一句话评语
     */
    public record Dimension(String name, int score, String comment) {
    }
}
