package io.github.xiaou61.knowledge.internal.question;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MasteryAndDiagnosisTest {

    @Test
    @DisplayName("答对提高掌握概率，答错降低，且都夹在 (0,1) 内")
    void bktMovesInTheRightDirection() {
        double prior = 0.3;
        double afterCorrect = MasteryModel.update(prior, true, 0.25);
        double afterWrong = MasteryModel.update(prior, false, 0.25);

        assertThat(afterCorrect).isGreaterThan(prior);
        assertThat(afterWrong).isLessThan(prior);
        assertThat(afterCorrect).isLessThan(1.0).isGreaterThan(0.0);
        assertThat(afterWrong).isLessThan(1.0).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("档位：没作答是 unknown，0.8 以上才是 mastered")
    void tiers() {
        assertThat(MasteryModel.tier(0.99, 0)).isEqualTo("unknown");
        assertThat(MasteryModel.tier(0.79, 3)).isEqualTo("familiar");
        assertThat(MasteryModel.tier(0.80, 3)).isEqualTo("mastered");
        assertThat(MasteryModel.tier(0.2, 3)).isEqualTo("learning");
    }

    @Test
    @DisplayName("Elo：难题被答对时评分下调得更多，易题答错时上调得更少")
    void eloDirection() {
        double ability = ItemRating.ability(0.8);
        double hardAfterCorrect = ItemRating.updateRating(1600, true, ability);
        double easyAfterCorrect = ItemRating.updateRating(900, true, ability);

        // 答对 → 题目变容易；难题被答对的信息量更大，所以下调更多
        assertThat(hardAfterCorrect).isLessThan(1600);
        assertThat(easyAfterCorrect).isLessThan(900);
        assertThat(1600 - hardAfterCorrect).isGreaterThan(900 - easyAfterCorrect);

        // 答错 → 题目变难；难题被答错几乎是意料之中，所以上调更少
        double hardAfterWrong = ItemRating.updateRating(1600, false, ability);
        double easyAfterWrong = ItemRating.updateRating(900, false, ability);
        assertThat(hardAfterWrong).isGreaterThan(1600);
        assertThat(easyAfterWrong).isGreaterThan(900);
        assertThat(easyAfterWrong - 900).isGreaterThan(hardAfterWrong - 1600);
    }

    @Test
    @DisplayName("前置诊断：自身未掌握且前置已掌握时，建议该前置")
    void suggestsFrontierPrerequisite() {
        // A(1) 是 B(2) 的前置，B 是 C(3) 的前置
        List<PrerequisiteDiagnosis.Edge> edges = List.of(
                new PrerequisiteDiagnosis.Edge(1, 2),
                new PrerequisiteDiagnosis.Edge(2, 3));
        // A 已掌握，B 未掌握，C 答错
        var suggestions = PrerequisiteDiagnosis.suggest(3, Map.of(1L, 0.9, 3L, 0.2), edges, 3);

        assertThat(suggestions).isNotEmpty();
        assertThat(suggestions.get(0).conceptId()).isEqualTo(2L);
        assertThat(suggestions.get(0).path()).containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("前置也没掌握时继续往前找，不会把中间节点当建议")
    void walksFurtherWhenParentIsAlsoWeak() {
        List<PrerequisiteDiagnosis.Edge> edges = List.of(
                new PrerequisiteDiagnosis.Edge(1, 2),
                new PrerequisiteDiagnosis.Edge(2, 3));
        var suggestions = PrerequisiteDiagnosis.suggest(3, Map.of(1L, 0.9, 2L, 0.2), edges, 3);

        assertThat(suggestions).extracting(PrerequisiteDiagnosis.Suggestion::conceptId)
                .containsExactly(2L);
    }

    @Test
    @DisplayName("没有前置返回空，不抛异常")
    void noEdgesReturnsEmpty() {
        assertThat(PrerequisiteDiagnosis.suggest(3, Map.of(), List.of(), 3)).isEmpty();
    }
}
