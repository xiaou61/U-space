package io.github.xiaou61.ai.internal.camp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 聚合、弱项、时长与对比（WORK-029 TASK-005/007）。
 */
class ScoreAndCompareTest {

    private static TopicScore.TopicResult topic(String name, int correct, int total,
                                                int hardCorrect, int hardTotal) {
        return new TopicScore.TopicResult(name, correct, total,
                Map.of("hard", new TopicScore.Difficulty(hardCorrect, hardTotal)));
    }

    // ── 聚合 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("按主题 × 难度聚合，主题按键排序（与输入顺序无关）")
    void aggregateGroupsByTopicAndTier() {
        Map<String, TopicScore.TopicResult> result = TopicScore.aggregate(List.of(
                new TopicScore.ScoredItem("并发", "easy", true),
                new TopicScore.ScoredItem("并发", "hard", false),
                new TopicScore.ScoredItem("集合", "medium", true)));

        assertThat(result.keySet()).containsExactly("并发", "集合");
        assertThat(result.get("并发").total()).isEqualTo(2);
        assertThat(result.get("并发").correct()).isEqualTo(1);
        assertThat(result.get("并发").of("hard")).isEqualTo(new TopicScore.Difficulty(0, 1));
        assertThat(result.get("并发").ratio()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("两次聚合结果逐项相等，题量为 0 的主题不出现")
    void aggregateIsDeterministicAndSkipsEmpty() {
        List<TopicScore.ScoredItem> items = List.of(new TopicScore.ScoredItem("集合", "easy", true));
        assertThat(TopicScore.aggregate(items)).isEqualTo(TopicScore.aggregate(items));
        assertThat(TopicScore.aggregate(List.of())).isEmpty();
    }

    // ── 弱项 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("四条判据各一例：错两题、比例低于 0.8、难档全错、强项")
    void weaknessRulesFourCases() {
        // 4 题错 2 → MULTI_WRONG
        assertThat(WeaknessRules.classify(topic("A", 2, 4, 1, 2)))
                .isEqualTo(new WeaknessRules.Verdict(true, WeaknessRules.Reason.MULTI_WRONG));
        // 5 题错 1 → ratio 0.8，不弱
        assertThat(WeaknessRules.classify(topic("B", 4, 5, 1, 2)).weak()).isFalse();
        // 2 题错 1 → ratio 0.5，LOW_RATIO（题量少时一题就是 25% 的信息）
        assertThat(WeaknessRules.classify(topic("C", 1, 2, 0, 1)))
                .isEqualTo(new WeaknessRules.Verdict(true, WeaknessRules.Reason.LOW_RATIO));
        // 5 题错 1 但难档全错 → HARD_ALL_WRONG
        assertThat(WeaknessRules.classify(topic("D", 4, 5, 0, 2)))
                .isEqualTo(new WeaknessRules.Verdict(true, WeaknessRules.Reason.HARD_ALL_WRONG));
        // 全对 → STRONG
        assertThat(WeaknessRules.classify(topic("E", 4, 4, 2, 2)))
                .isEqualTo(new WeaknessRules.Verdict(false, WeaknessRules.Reason.STRONG));
    }

    @Test
    @DisplayName("单题错一次就算弱；题量 4 时错一题不算")
    void singleWrongInFourIsNotWeak() {
        assertThat(WeaknessRules.classify(topic("X", 3, 4, 1, 1)).weak()).isFalse();
        assertThat(WeaknessRules.classify(topic("Y", 0, 1, 0, 0)).weak()).isTrue();
    }

    // ── 时长 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("阅读时长按 400 字/分钟、夹在 5..25，含交互块多 10 分钟")
    void readingMinutes() {
        assertThat(ReadingMinutes.of(0, false)).isEqualTo(5);
        assertThat(ReadingMinutes.of(400, false)).isEqualTo(5);
        assertThat(ReadingMinutes.of(1200, false)).isEqualTo(5);
        assertThat(ReadingMinutes.of(4000, false)).isEqualTo(10);
        assertThat(ReadingMinutes.of(20000, false)).isEqualTo(25);
        assertThat(ReadingMinutes.of(400, true)).isEqualTo(15);
    }

    // ── 对比 ────────────────────────────────────────────────────────

    private static Map<String, TopicScore.TopicResult> scores(String topic, int correct, int total) {
        return Map.of(topic, new TopicScore.TopicResult(topic, correct, total,
                Map.of("hard", new TopicScore.Difficulty(0, 1))));
    }

    @Test
    @DisplayName("3/3 → 2/3 是退步，2/3 → 2/3 持平，1/3 → 2/3 进步")
    void comparisonVerdicts() {
        assertThat(CampComparer.compare(scores("A", 3, 3), scores("A", 2, 3)).topics().get(0).verdict())
                .isEqualTo(CampComparer.Verdict.WORSE);
        assertThat(CampComparer.compare(scores("A", 2, 3), scores("A", 2, 3)).topics().get(0).verdict())
                .isEqualTo(CampComparer.Verdict.SAME);
        assertThat(CampComparer.compare(scores("A", 1, 3), scores("A", 2, 3)).topics().get(0).verdict())
                .isEqualTo(CampComparer.Verdict.IMPROVED);
    }

    @Test
    @DisplayName("题量不同时不给结论，并说明原因")
    void mismatchedCountsAreInconclusive() {
        CampComparer.Comparison comparison = CampComparer.compare(scores("A", 2, 3), scores("A", 2, 4));

        assertThat(comparison.mismatched()).containsExactly("A");
        assertThat(comparison.topics().get(0).verdict()).isEqualTo(CampComparer.Verdict.INCONCLUSIVE);
        assertThat(comparison.topics().get(0).reason()).contains("题量不同");
        assertThat(CampComparer.summary(comparison)).isEqualTo(CampComparer.Verdict.INCONCLUSIVE);
    }

    @Test
    @DisplayName("只出现在一侧的主题单独列出，不参与结论")
    void onlyInOneSideIsListed() {
        CampComparer.Comparison comparison = CampComparer.compare(
                Map.of("A", new TopicScore.TopicResult("A", 1, 2, Map.of())),
                Map.of("B", new TopicScore.TopicResult("B", 1, 2, Map.of())));

        assertThat(comparison.onlyInDiagnostic()).containsExactly("A");
        assertThat(comparison.onlyInRetest()).containsExactly("B");
        assertThat(comparison.topics()).isEmpty();
    }
}
