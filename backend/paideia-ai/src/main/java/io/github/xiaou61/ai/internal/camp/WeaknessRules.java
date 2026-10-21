package io.github.xiaou61.ai.internal.camp;

/**
 * 弱项判据（WORK-029）。
 *
 * <p>**不参考模型的意见**——模型自评"他会不会"是幻觉温床，与 WORK-018 三桶分类同一纪律。
 *
 * <pre>
 * weak ⇐ 错题数 ≥ 2
 *      ∨ (题量 ≤ 3 ∧ 有错题)
 *      ∨ (有难档题 ∧ 难档全错)
 * strong ⇐ ¬weak ∧ 答对率 ≥ 0.8
 * </pre>
 *
 * <p><b>为什么第一条不是"答对率 &lt; 0.8"</b>：需求与验收用例都要求「4 道题错 1 道不判弱」，
 * 但 3/4 = 0.75 恰好低于 0.8——照字面实现会把一个好主题打成弱项，也就是评审焦点里点名的
 * 「一道坏题把整主题判弱」。所以把 80% 这条留给"强项"的展示口径，判弱改用**错题数**：
 * 错 2 道才是真的缺口。
 *
 * <p><b>题量 ≤ 3 时错 1 道仍判弱</b>：题量太少时一道题就是 25–50% 的信息，不能忽略。
 * 「错一题不判弱」只在题量 ≥ 4 时成立，不属于无条件承诺。
 *
 * <p>纯函数。
 */
public final class WeaknessRules {

    /** 强项的展示口径。与 {@code LearningProgressRules} 的通过比例同一个 0.8。 */
    static final double PASS_RATIO = 0.8;
    /** 错这么多题就是弱，看数量不看比例（10 题错 2 题比例 0.8，仍是明显缺口）。 */
    static final int MULTI_WRONG = 2;
    /** 少于这么多题时，错 1 道也算弱。 */
    static final int SMALL_SET = 3;

    public enum Reason {
        MULTI_WRONG,
        LOW_RATIO,
        HARD_ALL_WRONG,
        /** 不算弱，也没到强项（例如 3/4）。 */
        OK,
        STRONG
    }

    public record Verdict(boolean weak, Reason reason) {
    }

    private WeaknessRules() {
    }

    public static Verdict classify(TopicScore.TopicResult topic) {
        if (topic == null || topic.total() == 0) {
            return new Verdict(false, Reason.OK);
        }
        int wrong = topic.total() - topic.correct();
        if (wrong >= MULTI_WRONG) {
            return new Verdict(true, Reason.MULTI_WRONG);
        }
        if (wrong >= 1 && topic.total() <= SMALL_SET) {
            return new Verdict(true, Reason.LOW_RATIO);
        }
        TopicScore.Difficulty hard = topic.of("hard");
        if (hard.total() > 0 && hard.correct() == 0) {
            return new Verdict(true, Reason.HARD_ALL_WRONG);
        }
        return new Verdict(false, topic.ratio() >= PASS_RATIO ? Reason.STRONG : Reason.OK);
    }

    /** 强项：不弱且答对率达到 0.8。界面用它把"不用学"的主题单独列出来。 */
    public static boolean strong(TopicScore.TopicResult topic) {
        return topic != null && topic.total() > 0 && !classify(topic).weak()
                && topic.ratio() >= PASS_RATIO;
    }
}
