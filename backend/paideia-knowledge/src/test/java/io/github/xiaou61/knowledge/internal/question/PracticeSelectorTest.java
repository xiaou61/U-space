package io.github.xiaou61.knowledge.internal.question;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PracticeSelectorTest {

    private static PracticeSelector.Candidate candidate(String key, double difficulty, int attempts) {
        return new PracticeSelector.Candidate(1, "quiz", key, key, difficulty, attempts);
    }

    /**
     * 选**目标难度**（能力下方 {@value PracticeSelector#TARGET_GAP} 分）最接近的题。
     *
     * <p>这条改过（2026-10-23）：原来断的是"难度等于能力"，而那是 50% 答对率、
     * BKT 下学得最慢的选择。现在目标难度在能力下方 150 分（答对率约 70%）。
     */
    @Test
    @DisplayName("选难度最接近目标难度（能力下方 150 分）的那道题")
    void picksClosestToTargetDifficulty() {
        // 能力 1200 → 目标 1050。三个候选里 1050 最近，1200 差 150，600 差 450
        var chosen = PracticeSelector.choose(List.of(
                candidate("too-easy", 600, 10),
                candidate("at-target", 1050, 10),
                candidate("at-ability", 1200, 10)), 1200);

        assertThat(chosen).hasValueSatisfying(value ->
                assertThat(value.questionKey()).isEqualTo("at-target"));
    }

    /** 目标难度确实不是"等于能力"——否则上面那条会因为两个候选都合理而变得含糊。 */
    @Test
    @DisplayName("难度等于能力的题不是最优选")
    void abilityLevelQuestionIsNotPreferred() {
        var chosen = PracticeSelector.choose(List.of(
                candidate("at-ability", 1200, 10),
                candidate("at-target", 1050, 10)), 1200);

        assertThat(chosen).hasValueSatisfying(value ->
                assertThat(value.questionKey())
                        .as("难度等于能力意味着 50%% 答对率，BKT 下学得最慢")
                        .isEqualTo("at-target"));
    }

    @Test
    @DisplayName("未校准的题拿到探索加分，但只在难度接近时才翻盘")
    void explorationBonusOnlyWhenClose() {
        // 难度 1200 未校准 vs 难度 1220 已校准：分数差在 0.25 以内，探索加分应让前者胜出
        var close = PracticeSelector.choose(List.of(
                candidate("calibrated", 1220, 10),
                candidate("new", 1200, 0)), 1200);
        assertThat(close).hasValueSatisfying(value ->
                assertThat(value.questionKey()).isEqualTo("new"));

        // 难度差 600 时，即使未校准也不该胜出
        var far = PracticeSelector.choose(List.of(
                candidate("calibrated", 1200, 10),
                candidate("new", 1800, 0)), 1200);
        assertThat(far).hasValueSatisfying(value ->
                assertThat(value.questionKey()).isEqualTo("calibrated"));
    }

    @Test
    @DisplayName("没有候选返回空")
    void emptyCandidates() {
        assertThat(PracticeSelector.choose(List.of(), 1200)).isEmpty();
    }
}
