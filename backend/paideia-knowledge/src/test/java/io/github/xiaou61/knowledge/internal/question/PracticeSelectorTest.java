package io.github.xiaou61.knowledge.internal.question;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PracticeSelectorTest {

    private static PracticeSelector.Candidate candidate(String key, double difficulty, int attempts) {
        return new PracticeSelector.Candidate(1, "quiz", key, key, difficulty, attempts);
    }

    @Test
    @DisplayName("选难度最接近当前能力的那道题")
    void picksClosestDifficulty() {
        var chosen = PracticeSelector.choose(List.of(
                candidate("far-easy", 600, 10),
                candidate("near", 1200, 10),
                candidate("far-hard", 1900, 10)), 1200);

        assertThat(chosen).hasValueSatisfying(value ->
                assertThat(value.questionKey()).isEqualTo("near"));
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