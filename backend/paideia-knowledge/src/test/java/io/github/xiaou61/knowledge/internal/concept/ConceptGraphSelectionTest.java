package io.github.xiaou61.knowledge.internal.concept;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 超过节点上限时的截断规则（WORK-021 AC-009）。
 *
 * <p>截断是**有损**的：被丢掉的节点在界面上就是不存在。所以规则必须是"留下有用的"，
 * 而不是"留下 id 小的"——后者会把用户真正学过的东西挤出视野。
 */
class ConceptGraphSelectionTest {

    private static ConceptGraphService.Node node(long id, boolean touched, double score) {
        return new ConceptGraphService.Node(
                id, "概念" + id, null, null, null,
                score, ConceptMastery.TIER_LEARNING, false,
                new ConceptGraphService.Evidence(touched ? 1 : 0, null, touched ? 1 : 0),
                List.of());
    }

    @Test
    @DisplayName("未超上限时原样返回，顺序不动")
    void belowLimitKeepsOrder() {
        List<ConceptGraphService.Node> input = List.of(node(3, false, 0), node(1, true, 0.9));
        assertThat(ConceptGraphService.selectNodes(input, 10)).isEqualTo(input);
    }

    @Test
    @DisplayName("超上限时先保留有证据的节点，即使它们 id 更大")
    void evidencedNodesWinOverLowIds() {
        List<ConceptGraphService.Node> input = new ArrayList<>();
        for (long id = 1; id <= 10; id++) {
            input.add(node(id, false, 0));
        }
        // 两条"学过"的节点，id 在最大一端
        input.add(node(90, true, 0.3));
        input.add(node(91, true, 0.5));

        List<ConceptGraphService.Node> kept = ConceptGraphService.selectNodes(input, 3);

        assertThat(kept).extracting(ConceptGraphService.Node::id)
                .containsExactly(91L, 90L, 1L);
    }

    @Test
    @DisplayName("同样有证据时分数高的在前")
    void higherScoreFirst() {
        List<ConceptGraphService.Node> input = List.of(
                node(1, true, 0.2), node(2, true, 0.9), node(3, true, 0.5));

        assertThat(ConceptGraphService.selectNodes(input, 2))
                .extracting(ConceptGraphService.Node::id)
                .containsExactly(2L, 3L);
    }

    @Test
    @DisplayName("分数也一样时按 id 升序，结果是确定的")
    void tiesAreDeterministic() {
        List<ConceptGraphService.Node> input = List.of(
                node(5, true, 0.5), node(2, true, 0.5), node(9, true, 0.5));

        assertThat(ConceptGraphService.selectNodes(input, 2))
                .extracting(ConceptGraphService.Node::id)
                .containsExactly(2L, 5L);
    }
}
