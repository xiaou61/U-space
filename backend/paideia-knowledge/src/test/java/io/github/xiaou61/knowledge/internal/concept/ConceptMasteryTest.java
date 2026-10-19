package io.github.xiaou61.knowledge.internal.concept;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xiaou61.knowledge.internal.assessment.SelfAssessment;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConceptMasteryTest {

    private static final Instant NOW = Instant.parse("2026-10-19T06:00:00Z");

    private static ConceptMastery.Evidence scored(double ratio) {
        return new ConceptMastery.Evidence(ratio, null, false, NOW);
    }

    private static ConceptMastery.Evidence assessed(String level) {
        return new ConceptMastery.Evidence(null, level, false, NOW);
    }

    private static ConceptMastery.Evidence viewed() {
        return new ConceptMastery.Evidence(null, null, true, NOW);
    }

    @Test
    @DisplayName("没有任何证据时是 0 分、未接触")
    void noEvidenceIsZeroAndUnknown() {
        ConceptMastery.Result result = ConceptMastery.compute(List.of(), false, NOW);

        assertThat(result.score()).isZero();
        assertThat(result.tier()).isEqualTo(ConceptMastery.TIER_UNKNOWN);
        assertThat(result.reviewDue()).isFalse();
    }

    @Test
    @DisplayName("全是空信号（没有分数、没有自评、没看过）也按未接触处理")
    void blankEvidenceIsUnknown() {
        ConceptMastery.Result result = ConceptMastery.compute(
                List.of(new ConceptMastery.Evidence(null, null, false, null)), false, NOW);

        assertThat(result.tier()).isEqualTo(ConceptMastery.TIER_UNKNOWN);
    }

    @Test
    @DisplayName("只浏览过 → 了解，且分数很低")
    void viewedOnlyIsLearning() {
        ConceptMastery.Result result = ConceptMastery.compute(List.of(viewed()), false, NOW);

        assertThat(result.tier()).isEqualTo(ConceptMastery.TIER_LEARNING);
        assertThat(result.score()).isLessThan(0.5);
    }

    @Test
    @DisplayName("自评懂但没有分数 → 熟悉")
    void selfUnderstoodIsFamiliar() {
        ConceptMastery.Result result = ConceptMastery.compute(
                List.of(assessed(SelfAssessment.LEVEL_UNDERSTOOD)), false, NOW);

        assertThat(result.tier()).isEqualTo(ConceptMastery.TIER_FAMILIAR);
    }

    @Test
    @DisplayName("分数满分且自评懂 → 掌握")
    void fullScoreWithUnderstoodIsMastered() {
        ConceptMastery.Result result = ConceptMastery.compute(
                List.of(new ConceptMastery.Evidence(1.0, SelfAssessment.LEVEL_UNDERSTOOD, true, NOW)),
                false, NOW);

        assertThat(result.tier()).isEqualTo(ConceptMastery.TIER_MASTERED);
        assertThat(result.score()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("只有满分分数、没有自评 → 熟悉（还差一次自证）")
    void fullScoreWithoutSelfIsFamiliar() {
        ConceptMastery.Result result = ConceptMastery.compute(List.of(scored(1.0)), false, NOW);

        assertThat(result.tier()).isEqualTo(ConceptMastery.TIER_FAMILIAR);
    }

    @Test
    @DisplayName("自评懂但分数为零，永远到不了掌握")
    void conflictingEvidenceNeverReachesMastered() {
        ConceptMastery.Result result = ConceptMastery.compute(
                List.of(new ConceptMastery.Evidence(0.0, SelfAssessment.LEVEL_UNDERSTOOD, false, NOW)),
                false, NOW);

        assertThat(result.tier()).isNotEqualTo(ConceptMastery.TIER_MASTERED);
        assertThat(result.tier()).isEqualTo(ConceptMastery.TIER_FAMILIAR);
    }

    @Test
    @DisplayName("自评不确定时降一档")
    void unsureSelfNeverMastered() {
        ConceptMastery.Result result = ConceptMastery.compute(
                List.of(new ConceptMastery.Evidence(1.0, SelfAssessment.LEVEL_UNSURE, false, NOW)),
                false, NOW);

        assertThat(result.tier()).isEqualTo(ConceptMastery.TIER_FAMILIAR);
    }

    @Test
    @DisplayName("两个载体条目的分数都进入平均，而不是只取一条")
    void scoreAveragesAcrossCarrierEntries() {
        ConceptMastery.Result both = ConceptMastery.compute(
                List.of(scored(1.0), scored(0.2)), false, NOW);
        ConceptMastery.Result onlyHigh = ConceptMastery.compute(List.of(scored(1.0)), false, NOW);

        assertThat(both.score()).isLessThan(onlyHigh.score());
        assertThat(both.tier()).isEqualTo(ConceptMastery.TIER_LEARNING);
    }

    @Test
    @DisplayName("复习到期不把掌握度清零")
    void reviewDueDoesNotZeroTheScore() {
        ConceptMastery.Result result = ConceptMastery.compute(
                List.of(new ConceptMastery.Evidence(1.0, SelfAssessment.LEVEL_UNDERSTOOD, true, NOW)),
                true, NOW);

        assertThat(result.reviewDue()).isTrue();
        assertThat(result.score()).isEqualTo(1.0);
        assertThat(result.tier()).isEqualTo(ConceptMastery.TIER_MASTERED);
    }

    @Test
    @DisplayName("同一份数据算两次结果逐位相同")
    void sameInputIsDeterministic() {
        List<ConceptMastery.Evidence> evidences = List.of(scored(0.7), assessed("unsure"));
        Instant old = NOW.minus(45, ChronoUnit.DAYS);
        List<ConceptMastery.Evidence> withOld = List.of(
                new ConceptMastery.Evidence(0.7, "unsure", true, old));

        assertThat(ConceptMastery.compute(evidences, false, NOW))
                .isEqualTo(ConceptMastery.compute(evidences, false, NOW));
        assertThat(ConceptMastery.compute(withOld, false, NOW))
                .isEqualTo(ConceptMastery.compute(withOld, false, NOW));
    }

    @Test
    @DisplayName("时间越久分数越低，但有不低于 0.5 的下限")
    void decayLowersScoreButIsBounded() {
        double fresh = ConceptMastery.compute(
                List.of(new ConceptMastery.Evidence(1.0, null, true, NOW)), false, NOW).score();
        double stale = ConceptMastery.compute(
                List.of(new ConceptMastery.Evidence(1.0, null, true, NOW.minus(60, ChronoUnit.DAYS))),
                false, NOW).score();
        double ancient = ConceptMastery.compute(
                List.of(new ConceptMastery.Evidence(1.0, null, true, NOW.minus(500, ChronoUnit.DAYS))),
                false, NOW).score();

        assertThat(stale).isLessThan(fresh);
        assertThat(ancient).isGreaterThanOrEqualTo(fresh * 0.5 - 1e-9);
    }

    @Test
    @DisplayName("超出 0–1 的分数被夹住，不会算出负分或大于 1")
    void outOfRangeScoreIsClamped() {
        assertThat(ConceptMastery.compute(List.of(scored(1.8)), false, NOW).score()).isLessThanOrEqualTo(1.0);
        assertThat(ConceptMastery.compute(List.of(scored(-0.5)), false, NOW).score()).isGreaterThanOrEqualTo(0.0);
    }
}
