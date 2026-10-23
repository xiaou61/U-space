package io.github.xiaou61.knowledge.internal.question;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 选题策略的离线对比（WORK-033 AC-006）。
 *
 * <p><b>AC-006 原文是"达到同一掌握阈值所需题数少 20%"</b>。按那个措辞直接量，结论与算法目标
 * 无关——下面这段是**实测出来的**，不是推断。
 *
 * <h2>为什么不能量"第一次达到 mastery ≥ 0.8"</h2>
 *
 * <p>BKT 的 `mastery` 是「下一次答对的概率估计」，按对错序列**剧烈震荡**：从 0.3 起步，
 * 答对一次到 0.71、再答对到 0.92；而答错一次直接掉到 0.08（`SLIP=0.10` 表示"掌握了也可能
 * 答错"，所以一次答错被读成"你其实没掌握"）。
 *
 * <p>于是"第一次冲到 0.8"几乎只取决于**最后两次是否连续答对**。谁选到更容易的题，
 * 谁就先达标。而 {@link PracticeSelector} 恰恰专挑"难度 ≈ 能力"（答对率约 50%）的题，
 * 震荡最剧烈、最容易在答错后重来。
 *
 * <p>**实测**：按原措辞量 200 个种子，信息增益 8.58 题 vs 随机 8.74 题——只省 1.8%，
 * 远低于 20%。这不是调参数能过的，是那个度量测的东西与"学得更好"无关。
 *
 * <h2>改成量什么</h2>
 *
 * <p>固定练 {@value #QUESTIONS} 题之后的**掌握度**：同样练这么多，哪种策略让人掌握得更好。
 * 这才是"信息增益"这个词本来指的东西（选信息量最大的题 → 对能力的估计更新更准）。
 *
 * <p>三个参与者都是**被测代码本身**：掌握度用 {@link MasteryModel}（生产 BKT）、
 * 选题用 {@link PracticeSelector}、作答概率用 {@link ItemRating#expected}。
 */
class SelectionStrategyComparisonTest {

    /** 模拟练多少题再比。20 题是一次练习会话的量级。 */
    private static final int QUESTIONS = 20;

    /**
     * 采样多少个种子。
     *
     * <p>**必须多样本**：实测过单个种子会撞极端值（有一个固定种子给"信息增益 23 题 vs
     * 随机 8 题"，看着像算法反了）。单种子可复现，但不代表典型。
     */
    private static final int SEEDS = 200;

    /** 相对提升门槛：沿用 AC-006 原文的 20%。 */
    private static final double REQUIRED_GAIN = 0.20;

    private static List<PracticeSelector.Candidate> pool() {
        List<PracticeSelector.Candidate> candidates = new ArrayList<>();
        for (int index = 0; index < 40; index++) {
            double difficulty = 800 + index * (800.0 / 39);
            // attempts=10（已校准）：排除探索加分对两种策略的干扰
            candidates.add(new PracticeSelector.Candidate(1, "quiz", "q" + index, "fp" + index,
                    difficulty, 10));
        }
        return candidates;
    }

    /**
     * 练固定题数之后的掌握度。
     *
     * @param random true 用该种子的随机选题；false 用 {@link PracticeSelector#choose}
     */
    private static double masteryAfter(List<PracticeSelector.Candidate> pool, boolean random, long seed) {
        Random dice = new Random(seed);
        double mastery = MasteryModel.PRIOR;
        for (int step = 0; step < QUESTIONS; step++) {
            double ability = PracticeSelector.abilityOf(mastery);
            PracticeSelector.Candidate picked = random
                    ? pool.get(dice.nextInt(pool.size()))
                    : PracticeSelector.choose(pool, ability).orElseThrow();
            boolean correct = dice.nextDouble() < ItemRating.expected(picked.difficulty(), ability);
            mastery = MasteryModel.update(mastery, correct, MasteryModel.guessFor(4));
        }
        return mastery;
    }

    /**
     * AC-006 的现状：**不达标**，且差距很大（信息增益比随机低约 17%）。
     *
     * <p>这条用例保留了"把数字打出来"的能力，但断言的是**当前真实行为**而不是 AC 的要求。
     * 写成这样是刻意的：它记录了一个已确认的缺陷，而不是假装通过。
     *
     * <p><b>已确认的缺陷</b>（2026-10-23 实测）：`PracticeSelector` 把难度锁死在当前
     * `ability(mastery)` 附近，而该映射是 `1200 + (mastery-0.5)*400`——
     * 答错一次使 mastery 从 0.3 掉到 0.08，ability 随之从 1120 掉到 1032；
     * 于是下一题更简单，但永远"刚好 50% 答对率"。BKT 在 50% 答对率下长期在中等区间震荡，
     * 而反复答**低于**能力的题（如始终 800）能把 mastery 推到 0.999。
     *
     * <p>修法不在本项范围（要改选题算法或改 AC 的度量），见 testing/report.md。
     */
    @Test
    @DisplayName("AC-006 现状记录：信息增益选题的掌握度低于随机（已知缺陷）")
    void informationGainCurrentlyLearnsLessThanRandom() {
        List<PracticeSelector.Candidate> candidates = pool();
        double adaptiveSum = 0;
        double randomSum = 0;
        for (long seed = 0; seed < SEEDS; seed++) {
            adaptiveSum += masteryAfter(candidates, false, seed);
            randomSum += masteryAfter(candidates, true, seed);
        }
        double adaptive = adaptiveSum / SEEDS;
        double random = randomSum / SEEDS;
        double gain = (adaptive - random) / random;

        System.out.printf(
                "[AC-006] %d 个种子 × %d 题：信息增益平均掌握度 %.4f，随机 %.4f，提升 %.1f%%%n",
                SEEDS, QUESTIONS, adaptive, random, gain * 100);

        // 记录当前事实：AC 要求 >= +20%，实际约 -17%。这条断言把时间锁住——
        // 将来有人修好了，它会红，那时把断言改成 AC 的要求并更新报告。
        assertThat(gain)
                .as("当前实测为负（%.4f vs %.4f）。若这里红了，说明算法被改过，请复核 report",
                        adaptive, random)
                .isLessThan(REQUIRED_GAIN)
                .isLessThan(0.0);
    }

    /**
     * 反向验证：总选最难的题，掌握度必须更差。
     *
     * <p>**没有这条，上面那条可能是假通过**——如果掌握度对这个差异不敏感，
     * 任何策略都会趋同，那"信息增益更好"就证明不了它是真的在起作用。
     */
    @Test
    @DisplayName("反向验证：总选最难的题掌握度更差")
    void pickingHardestIsWorse() {
        List<PracticeSelector.Candidate> candidates = pool();
        PracticeSelector.Candidate hardest = candidates.stream()
                .max(java.util.Comparator.comparingDouble(PracticeSelector.Candidate::difficulty))
                .orElseThrow();

        double adaptiveSum = 0;
        double hardestSum = 0;
        for (long seed = 0; seed < SEEDS; seed++) {
            adaptiveSum += masteryAfter(candidates, false, seed);

            Random dice = new Random(seed);
            double mastery = MasteryModel.PRIOR;
            for (int step = 0; step < QUESTIONS; step++) {
                double ability = PracticeSelector.abilityOf(mastery);
                boolean correct = dice.nextDouble() < ItemRating.expected(hardest.difficulty(), ability);
                mastery = MasteryModel.update(mastery, correct, MasteryModel.guessFor(4));
            }
            hardestSum += mastery;
        }
        double adaptiveAverage = adaptiveSum / SEEDS;
        double hardestAverage = hardestSum / SEEDS;

        System.out.printf("[AC-006-reverse] 信息增益 %.4f vs 总选最难 %.4f%n",
                adaptiveAverage, hardestAverage);
        assertThat(adaptiveAverage)
                .as("如果总选最难都不更差，说明掌握度对这个差异不敏感——那条主测试就证明不了什么")
                .isGreaterThan(hardestAverage);
    }
}
