package io.github.xiaou61.knowledge.internal.concept;

import io.github.xiaou61.knowledge.internal.assessment.SelfAssessment;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 掌握度计算（WORK-021）。
 *
 * <p><b>纯函数</b>：输入是证据列表，输出是分数与档位，不碰数据库、不读时钟以外的任何状态。
 * 这样它能在毫秒内被单测穷举，也能在将来整体替换成 BKT —— 接口不变，库不动，前端不动。
 *
 * <p>为什么现在不用 BKT：知识追踪的参数靠同一知识点的**多条作答序列**拟合，而一个概念平均
 * 只有三两道小测题，样本量个位数；参考实现 pyBKT 是按数百名学生、数千次作答的规模做基准的，
 * 且是 Python。当前用加权平均 + 时间衰减，是"信号质量够不上模型复杂度"时的诚实选择。
 *
 * <p><b>分数与档位分开</b>：分数负责着色与排序（连续），档位负责给用户一句话（离散）。
 * 混在一起会让"0.79 和 0.81 应该是同一档"这类判断变成魔法阈值。
 */
public final class ConceptMastery {

    /** 档位取值。与前端着色表一一对应，不要在别处再写字面量。 */
    public static final String TIER_UNKNOWN = "unknown";
    public static final String TIER_LEARNING = "learning";
    public static final String TIER_FAMILIAR = "familiar";
    public static final String TIER_MASTERED = "mastered";

    /** 两路**有测量含义**的信号的权重，只对实际存在的信号重新归一。 */
    private static final double WEIGHT_SCORE = 0.5;
    private static final double WEIGHT_SELF = 0.3;

    /** 自评"懂了"折算成的一个分数；它不是测量值，只是与其他两路量纲对齐。 */
    private static final double SELF_UNDERSTOOD = 1.0;
    private static final double SELF_UNSURE = 0.2;
    /**
     * 浏览只证明"接触过"，不证明会。它是**兜底信号**而不是加权项：
     * 把它算进加权平均会让"满分 + 读过"反而低于"满分"，那不成立。
     */
    private static final double VIEWED_VALUE = 0.4;

    /** 衰减半衰期与下限：两个月没碰不会把已通过的标签清零，只是明显降权。 */
    private static final double HALF_LIFE_DAYS = 30.0;
    private static final double MIN_DECAY = 0.5;

    /** 分数达到这个比例才可能进「熟悉」或「掌握」。与课程通过口径（0.8）一致。 */
    private static final double PASS_RATIO = 0.8;

    /**
     * 一路证据：某个载体条目上已知的东西。缺失的信号是 {@code null} / {@code false}，
     * 而不是 0 —— "没做过小测"和"小测得了 0 分"是两件完全不同的事。
     */
    public record Evidence(Double scoreRatio, String selfLevel, boolean viewed, Instant lastActivity) {
    }

    public record Result(double score, String tier, boolean reviewDue) {
    }

    private ConceptMastery() {
    }

    /**
     * 算一个概念的掌握度。
     *
     * @param evidences 该概念**所有载体条目**的证据；空列表表示这个概念完全没有被接触过
     * @param reviewDue 由调用方按 {@code review_due_at} 判定——它属于复习调度，不属于掌握度
     * @param now       算衰减的基准时刻，由调用方传入而不是内部取时钟，这样函数是纯的
     */
    public static Result compute(List<Evidence> evidences, boolean reviewDue, Instant now) {
        if (evidences == null || evidences.isEmpty()) {
            return new Result(0, TIER_UNKNOWN, reviewDue);
        }

        double sum = 0;
        int counted = 0;
        boolean anyScore = false;
        boolean allScoresPass = true;
        boolean hasSelf = false;
        boolean anyUnsure = false;

        for (Evidence evidence : evidences) {
            if (evidence == null || !hasSignal(evidence)) {
                continue;
            }
            Double ratio = evidence.scoreRatio();
            double weighted = 0;
            double weight = 0;
            if (ratio != null) {
                weighted += WEIGHT_SCORE * clamp01(ratio);
                weight += WEIGHT_SCORE;
                anyScore = true;
                if (ratio < PASS_RATIO) {
                    allScoresPass = false;
                }
            }
            String level = evidence.selfLevel();
            if (SelfAssessment.LEVEL_UNDERSTOOD.equals(level)) {
                weighted += WEIGHT_SELF * SELF_UNDERSTOOD;
                weight += WEIGHT_SELF;
                hasSelf = true;
            } else if (SelfAssessment.LEVEL_UNSURE.equals(level)) {
                weighted += WEIGHT_SELF * SELF_UNSURE;
                weight += WEIGHT_SELF;
                hasSelf = true;
                anyUnsure = true;
            }
            // 浏览是兜底：有分数或自评时它不参与，只有都没有时才算数
            sum += weight > 0 ? weighted / weight : VIEWED_VALUE;
            counted++;
        }

        if (counted == 0) {
            return new Result(0, TIER_UNKNOWN, reviewDue);
        }

        double raw = sum / counted;
        double score = clamp01(raw * decay(evidences, now));
        return new Result(score, tierOf(anyScore, allScoresPass, hasSelf, anyUnsure), reviewDue);
    }

    /**
     * 档位规则（与分数解耦，便于逐条单测）：
     *
     * <ul>
     *   <li>没有任何有信号的证据 → unknown</li>
     *   <li>有分数且每条都 ≥ 0.8，且自评过且都是"懂" → mastered</li>
     *   <li>自评懂，或所有分数都 ≥ 0.8 → familiar</li>
     *   <li>其余 → learning</li>
     * </ul>
     *
     * <p>关键一条：**任何一条"不确定"都会把掌握压回熟悉**。自评说会、分数为零是自相矛盾的
     * 证据，系统没有理由替用户相信自己。
     */
    private static String tierOf(boolean anyScore, boolean allScoresPass, boolean hasSelf, boolean anyUnsure) {
        boolean scoresPass = anyScore && allScoresPass;
        boolean selfPasses = hasSelf && !anyUnsure;
        if (scoresPass && selfPasses) {
            return TIER_MASTERED;
        }
        if (scoresPass || selfPasses) {
            return TIER_FAMILIAR;
        }
        return TIER_LEARNING;
    }

    private static boolean hasSignal(Evidence evidence) {
        return evidence.scoreRatio() != null
                || SelfAssessment.LEVEL_UNDERSTOOD.equals(evidence.selfLevel())
                || SelfAssessment.LEVEL_UNSURE.equals(evidence.selfLevel())
                || evidence.viewed();
    }

    /** 取最近一次活动算衰减；没有任何时间戳就不衰减。 */
    private static double decay(List<Evidence> evidences, Instant now) {
        Instant latest = null;
        for (Evidence evidence : evidences) {
            if (evidence == null || evidence.lastActivity() == null) {
                continue;
            }
            if (latest == null || evidence.lastActivity().isAfter(latest)) {
                latest = evidence.lastActivity();
            }
        }
        if (latest == null || now == null) {
            return 1.0;
        }
        long days = Math.max(0, Duration.between(latest, now).toDays());
        double factor = Math.pow(0.5, days / HALF_LIFE_DAYS);
        return Math.max(MIN_DECAY, factor);
    }

    private static double clamp01(double value) {
        if (Double.isNaN(value)) {
            return 0;
        }
        return Math.max(0, Math.min(1, value));
    }
}
