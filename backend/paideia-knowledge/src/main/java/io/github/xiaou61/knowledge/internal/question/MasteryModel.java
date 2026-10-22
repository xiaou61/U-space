package io.github.xiaou61.knowledge.internal.question;

/**
 * 知识追踪（BKT）的纯函数实现（WORK-033）。
 *
 * <p>每个「用户 × 概念」维护一个掌握概率，答对/答错后按贝叶斯规则更新。与旧的加权平均相比，
 * 它多出的唯一能力是**随作答序列演化**——这是「下一次答对的概率」能算出来的前提。
 *
 * <p>参数集中在常量里，换成 IRT 时只改这个类，调用方不动。数值都是经验先验，不是拟合结果：
 * 线上目前没有作答数据，任何"调好的参数"都是假的。
 */
public final class MasteryModel {

    /** 初始掌握概率：没见过这个概念时假设三成。 */
    static final double PRIOR = 0.3;
    /** 每次练习的学习率：答完一次，没掌握的补上 15%。 */
    static final double LEARN = 0.15;
    /** 失误率：掌握了也可能答错（手滑/看错题）。 */
    static final double SLIP = 0.10;
    /** 默认猜对率：选项数未知时按四选一。 */
    static final double DEFAULT_GUESS = 0.25;

    private MasteryModel() {
    }

    /**
     * 更新一次掌握概率。
     *
     * <p>先按「学习」把概率往上抬，再按观测（对/错）做贝叶斯修正。这是标准 BKT 的四步中的两步；
     * 遗忘（forget）暂时不建模——课程侧的复习衰减由 `review_due_at` 另行负责。
     */
    static double update(double prior, boolean correct, double guess) {
        double p = clamp01(prior);
        double g = clamp01(guess);
        double afterLearn = p + (1 - p) * LEARN;
        double posterior;
        if (correct) {
            posterior = afterLearn * (1 - SLIP)
                    / (afterLearn * (1 - SLIP) + (1 - afterLearn) * g);
        } else {
            posterior = afterLearn * SLIP
                    / (afterLearn * SLIP + (1 - afterLearn) * (1 - g));
        }
        return Math.max(0.001, Math.min(0.999, posterior));
    }

    /** 单选题的猜对率：选项越少越容易蒙对。 */
    static double guessFor(int optionCount) {
        if (optionCount < 2 || optionCount > 6) {
            return DEFAULT_GUESS;
        }
        return 1.0 / optionCount;
    }

    /**
     * 档位。**没作答过就是 unknown**——不用群体均值伪装成个人结论。
     *
     * <p>阈值与课程通过口径一致（0.8）；0.6 是「熟悉」的下限。
     */
    public static String tier(double mastery, int attempts) {
        if (attempts <= 0) {
            return "unknown";
        }
        if (mastery >= 0.80) {
            return "mastered";
        }
        return mastery >= 0.60 ? "familiar" : "learning";
    }

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }
}
