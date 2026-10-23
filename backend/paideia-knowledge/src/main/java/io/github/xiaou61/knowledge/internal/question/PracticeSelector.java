package io.github.xiaou61.knowledge.internal.question;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 练习选题（WORK-033）：在给定概念集合里挑“信息量最大”的一题。
 *
 * <p>打分 = 难度贴近当前能力（信息函数）+ 未校准题目的探索加分。
 * 不排序、不推荐整组——返回一道题，调用方决定怎么展示。
 *
 * <p>信息函数用 `1 / (1 + ((难度-能力)/200)^2)`：难度与能力相等时最接近 1，
 * 偏离 200 分时降到 0.5。它不需要 IRT 的参数拟合，冷启动也能用。
 */
final class PracticeSelector {

    /** 未校准（作答少于 5 次）的题目额外加分，鼓励探索。 */
    static final double EXPLORE_BONUS = 0.25;
    static final int CALIBRATED_ATTEMPTS = 5;

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
        double gap = (candidate.difficulty() - ability) / 200.0;
        double information = 1.0 / (1.0 + gap * gap);
        return information + (candidate.attempts() < CALIBRATED_ATTEMPTS ? EXPLORE_BONUS : 0);
    }

    /** 能力分：与 `ItemRating.ability` 同一映射，避免两处各写一套。 */
    static double abilityOf(double mastery) {
        return ItemRating.ability(mastery);
    }
}