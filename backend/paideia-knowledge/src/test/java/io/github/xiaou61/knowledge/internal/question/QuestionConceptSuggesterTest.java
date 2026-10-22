package io.github.xiaou61.knowledge.internal.question;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuestionConceptSuggesterTest {

    private static final List<QuestionConceptSuggester.ConceptText> CONCEPTS = List.of(
            new QuestionConceptSuggester.ConceptText(1, "索引", "B+ 树与最左前缀"),
            new QuestionConceptSuggester.ConceptText(2, "事务", "ACID 与隔离级别"),
            new QuestionConceptSuggester.ConceptText(3, "分页", "LIMIT 与深分页"));

    @Test
    @DisplayName("题干命中哪个概念，哪个排前面")
    void ranksMatchingConceptFirst() {
        var result = QuestionConceptSuggester.suggest(
                "联合索引的最左前缀是什么？", List.of("索引", "事务", "分页"),
                CONCEPTS, Set.of(), Map.of(), 3);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).conceptId()).isEqualTo(1L);
        assertThat(result.get(0).hits()).isNotEmpty();
    }

    @Test
    @DisplayName("条目已关联的概念获得结构加分")
    void entryConceptGetsBonus() {
        // 题干里两个概念都只命中一点点，靠条目关联让「分页」胜出
        var result = QuestionConceptSuggester.suggest(
                "LIMIT 分页怎么写", List.of("深分页", "索引"),
                CONCEPTS, Set.of(3L), Map.of(), 3);

        assertThat(result.get(0).conceptId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("完全无关时返回空，不硬凑候选")
    void unrelatedReturnsEmpty() {
        var result = QuestionConceptSuggester.suggest(
                "zzz qqq", List.of("xxx", "yyy"), CONCEPTS, Set.of(), Map.of(), 5);

        assertThat(result).isEmpty();
    }
}
