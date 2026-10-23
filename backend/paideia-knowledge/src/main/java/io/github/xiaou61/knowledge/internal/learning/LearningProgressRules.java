package io.github.xiaou61.knowledge.internal.learning;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 通过判定与复习排期的**纯函数**（WORK-020）。
 *
 * <p>单独拎出来是为了能不看数据库地测边界：0 分、刚好 80%、缺一类记录、复习阶梯到顶。
 * 通过判定**只在服务端做**——客户端只上报分数，不发 `passed`。
 */
public final class LearningProgressRules {

    /** 一个块的得分。`total` 是题数，不是分值。 */
    public record BlockScore(int score, int total) {
    }

    /** 通过门槛：每一项都至少 80%。整数比较，不用浮点。 */
    private static final int PASS_NUMERATOR = 8;
    private static final int PASS_DENOMINATOR = 10;

    /** 复习阶梯：通过后 1 / 3 / 7 / 21 天。到顶就不再增长。 */
    private static final int[] INTERVAL_DAYS = {1, 3, 7, 21};

    private LearningProgressRules() {
    }

    /**
     * 这一站算不算学会。
     *
     * <p>三条同时成立：有挑战记录、有小测记录、每一条都 ≥ 80%。**没有 total 的记录视为不通过**
     * ——空块（total=0）既不能算对也不能算 100%。
     */
    public static boolean passed(Map<String, BlockScore> challenges, Map<String, BlockScore> quizzes) {
        return !challenges.isEmpty() && !quizzes.isEmpty()
                && allStrong(challenges) && allStrong(quizzes);
    }

    private static boolean allStrong(Map<String, BlockScore> scores) {
        for (BlockScore score : scores.values()) {
            if (score.total() <= 0) {
                return false;
            }
            if ((long) score.score() * PASS_DENOMINATOR < (long) score.total() * PASS_NUMERATOR) {
                return false;
            }
        }
        return true;
    }

    /** 合并分数：同一块多次提交取**更高分**，重做一次手滑不会把已通过的降级。 */
    public static Map<String, BlockScore> merge(Map<String, BlockScore> previous, Map<String, BlockScore> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return previous;
        }
        Map<String, BlockScore> merged = new LinkedHashMap<>(previous);
        incoming.forEach((id, next) -> merged.merge(id, next,
                (old, candidate) -> candidate.total() == 0 || old.total() == 0
                        ? candidate
                        : candidate.score() * old.total() >= old.score() * candidate.total() ? candidate : old));
        return merged;
    }

    /** 再次通过时前进一档；到顶停在最后一档。 */
    public static int nextStage(int current) {
        return Math.min(Math.max(current, 0) + 1, INTERVAL_DAYS.length - 1);
    }

    /** 某一档的间隔天数。越界按边界处理，不抛错（脏数据不该让读路径失败）。 */
    public static int intervalDays(int stage) {
        int index = Math.min(Math.max(stage, 0), INTERVAL_DAYS.length - 1);
        return INTERVAL_DAYS[index];
    }

    public static Instant dueAt(Instant from, int stage) {
        return from.plus(Duration.ofDays(intervalDays(stage)));
    }

    /** 复习交卷后的新排期。答错档位不变、明天再来；全对才前进一档。 */
    public record ReviewOutcome(int stage, java.time.Instant dueAt) {
    }

    public static ReviewOutcome afterReview(int currentStage, boolean allCorrect, java.time.Instant now) {
        if (!allCorrect) {
            return new ReviewOutcome(Math.max(currentStage, 0), now.plus(Duration.ofDays(1)));
        }
        int stage = nextStage(currentStage);
        return new ReviewOutcome(stage, dueAt(now, stage));
    }

    /** 复习阶梯的总档数，供测试与文档引用。 */
    public static int stageCount() {
        return INTERVAL_DAYS.length;
    }
}
