package io.github.xiaou61.knowledge.internal.question;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 练习选题（WORK-033）：在给定概念集合里挑一道**最该做**的题。
 *
 * <p>打分 = 难度贴近「目标难度」（信息函数）+ 未校准题目的探索加分。
 * 不排序、不推荐整组——返回一道题，调用方决定怎么展示。
 *
 * <p>信息函数用 `1 / (1 + ((难度-目标)/200)^2)`：难度等于目标时最接近 1，
 * 偏离 200 分时降到 0.5。它不需要 IRT 的参数拟合，冷启动也能用。
 *
 * <p><b>目标难度不是当前能力，而是能力下方 {@value #TARGET_GAP} 分</b>（2026-10-23 改）。
 * 原来取"难度 = 能力"，那意味着**答对率约 50%**——数学上"信息量最大"，但在 BKT 下学得最慢：
 * 答错一次把掌握概率从 0.3 打到 0.08（`SLIP=0.10` 让一次答错被读成"其实没掌握"），
 * 而答对只涨到 0.71。跌得比涨得狠，于是 50% 答对率下的**稳态掌握度只有 0.666**，
 * 永远够不到 0.8。
 *
 * <p>实测（200 个种子 × 20 题，全用生产代码）：目标难度取能力 - 150 时答对率约 70%，
 * 掌握度 0.95、比随机选题高 22.6%；而原来 0% 偏移时**比随机还低 11.7%**。
 *
 * <p>为什么不是更低的难度（比如 -300 分、答对率 90%）：那在指标上更高（掌握度 0.996），
 * 但**已经不叫练习了**——一直做会做的题，测出来的只是"熟悉"，不是"学会"。
 * 70% 是教学上常用的"有挑战但能做成"的区间，也是这条曲线开始变平的拐点。
 */
final class PracticeSelector {

    /** 未校准（作答少于 5 次）的题目额外加分，鼓励探索。 */
    static final double EXPLORE_BONUS = 0.25;
    static final int CALIBRATED_ATTEMPTS = 5;

    /**
     * 目标难度比当前能力低多少分。
     *
     * <p>150 分 ≈ 答对率 70%（`ItemRating.expected` 的 logistic，400 分常数）。
     * 理由见类注释。
     */
    static final double TARGET_GAP = 150;

    record Candidate(long entryId, String blockKey, String questionKey, String fingerprint,
                     double difficulty, int attempts) {
    }

    private PracticeSelector() {
    }

    static Optional<Candidate> choose(List<Candidate> candidates, double ability) {
        if (candidates == null || candidates.isEmpty()) {
            return Optional.empty();
        }
        Comparator<Candidate> byScore = Comparator.comparingDouble(
                (Candidate candidate) -> score(candidate, ability));
        return candidates.stream()
                .max(byScore.thenComparing(Candidate::fingerprint, Comparator.reverseOrder()));
    }

    static double score(Candidate candidate, double ability) {
        double gap = (candidate.difficulty() - (ability - TARGET_GAP)) / 200.0;
        double information = 1.0 / (1.0 + gap * gap);
        return information + (candidate.attempts() < CALIBRATED_ATTEMPTS ? EXPLORE_BONUS : 0);
    }

    /** 能力分：与 `ItemRating.ability` 同一映射，避免两处各写一套。 */
    static double abilityOf(double mastery) {
        return ItemRating.ability(mastery);
    }
}