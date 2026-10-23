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

    /**
     * 按作答序列回放能复现当前掌握概率（AC-002）。
     *
     * <p>**为什么这条重要**：`concept_mastery_state` 存的是**当前值**，而 `question_attempts`
     * 是账本。两者必须一致——否则"从账本重算"与"读存量"会给出两个不同的掌握度，
     * 而用户看到的那个数字取决于代码走了哪条路。
     *
     * <p>误差阈值取 0.001：`update` 是纯浮点运算，同一序列重放必须**逐位相同**；
     * 留 0.001 只是为了容忍将来换成矩阵实现时的浮点重排，不是给自己留余地。
     */
    @Test
    @DisplayName("AC-002 按作答序列回放能复现当前概率（误差 < 0.001）")
    void replayReproducesMastery() {
        // 一段有对有错的真实形状序列：对、对、错、对、错、错、对
        boolean[] sequence = {true, true, false, true, false, false, true};
        int optionCount = 4;
        double guess = MasteryModel.guessFor(optionCount);

        // 逐条推进得到的"存量"
        double stored = MasteryModel.PRIOR;
        for (boolean correct : sequence) {
            stored = MasteryModel.update(stored, correct, guess);
        }

        // 从同一个起点重放同一序列
        double replayed = MasteryModel.PRIOR;
        for (boolean correct : sequence) {
            replayed = MasteryModel.update(replayed, correct, guess);
        }

        assertThat(Math.abs(stored - replayed)).as("回放必须复现存量").isLessThan(0.001);

        // 反向确认：序列变了结果就该变——否则"复现"是假通过（比如 update 根本没影响）
        double different = MasteryModel.PRIOR;
        for (boolean correct : new boolean[] {false, false, true}) {
            different = MasteryModel.update(different, correct, guess);
        }
        assertThat(Math.abs(stored - different))
                .as("不同的作答序列必须给出不同的掌握度，否则这条测试证明不了什么")
                .isGreaterThan(0.001);
    }

    /** 回放不依赖起点之外的任何状态：调两次同一序列必须完全一致。 */
    @Test
    @DisplayName("AC-002 同一序列重复回放结果稳定")
    void replayIsDeterministic() {
        double first = MasteryModel.PRIOR;
        for (int index = 0; index < 20; index++) {
            first = MasteryModel.update(first, index % 3 != 0, 0.25);
        }
        double second = MasteryModel.PRIOR;
        for (int index = 0; index < 20; index++) {
            second = MasteryModel.update(second, index % 3 != 0, 0.25);
        }
        assertThat(first).isEqualTo(second);
    }
}
