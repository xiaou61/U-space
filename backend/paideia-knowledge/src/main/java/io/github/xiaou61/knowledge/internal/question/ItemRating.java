package io.github.xiaou61.knowledge.internal.question;

/**
 * 题目难度的 Elo 更新（WORK-033）。
 *
 * <p>选 Elo 而不是 IRT：单次作答就能更新，不需要每题几百条样本；线上目前是零作答，
 * IRT 的参数无从估起。等某题作答数上到 200 再考虑换成 2PL。
 *
 * <p>用户的「能力分」不单独存表：由该概念的 BKT 掌握概率线性映射到 800–1600，
 * 这样能力与难度同一量纲，且不会多出一张需要同步的状态表。
 */
final class ItemRating {

    static final double INITIAL = 1200;
    /** 学习侧与题侧的更新速度。24 比国际象棋的 32 稳一些，小样本不至于跳得太凶。 */
    static final double K = 24;
    /** 低于这个作答数就算「未校准」，界面上要标明。 */
    static final int CALIBRATED_ATTEMPTS = 5;

    private ItemRating() {
    }

    /** 掌握概率 → 能力分。0.3（初始）对应 1120，刚好低于初始难度 1200，符合"新手遇到新题会错"。 */
    static double ability(double mastery) {
        return 1200 + (mastery - 0.5) * 400;
    }

    /** 期望正确率：Elo 的标准 logistic。 */
    static double expected(double rating, double ability) {
        return 1.0 / (1.0 + Math.pow(10, (rating - ability) / 400.0));
    }

    /**
     * 更新题目评分：答对说明题比预想的容易，评分下调；答错则上调。
     *
     * <p>方向容易写反，这里钉住：`actual - expected` 为正表示「不该答对却答对了」，
     * 题目应该更容易，所以 rating **减去** 它。
     */
    static double updateRating(double rating, boolean correct, double ability) {
        double actual = correct ? 1 : 0;
        double next = rating - K * (actual - expected(rating, ability));
        return Math.max(400, Math.min(2400, next));
    }
}
