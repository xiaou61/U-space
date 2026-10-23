package io.github.xiaou61.ai.internal.review;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xiaou61.knowledge.ReviewQuestionPort.Concept;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewQuestionGeneratorTest {

    private final ReviewQuestionGenerator generator = new ReviewQuestionGenerator(null, null, null, null);

    private static final List<Concept> CONCEPTS = List.of(
            new Concept(1, "索引", "B+ 树与最左前缀", "MySQL 索引"),
            new Concept(2, "事务", "ACID", "事务与锁"));

    @Test
    @DisplayName("结构合格的题保留，答案下标合法")
    void keepsValidQuestion() {
        String json = """
                {"items":[{"conceptId":1,"stem":"最左前缀指什么？",
                  "options":["A","B","C","D"],"answerIndex":2,"explanation":"从最左列开始匹配。"}]}
                """;

        var questions = generator.parse(json, CONCEPTS, Set.of());

        assertThat(questions).hasSize(1);
        assertThat(questions.get(0).answerIndex()).isEqualTo(2);
        assertThat(questions.get(0).fingerprint()).hasSize(64);
    }

    @Test
    @DisplayName("选项不足、下标越界、解析为空、概念不在集合里都丢弃")
    void dropsInvalidQuestions() {
        String json = """
                {"items":[
                  {"conceptId":1,"stem":"缺选项","options":["A","B"],"answerIndex":0,"explanation":"x"},
                  {"conceptId":1,"stem":"下标越界","options":["A","B","C","D"],"answerIndex":9,"explanation":"x"},
                  {"conceptId":1,"stem":"没解析","options":["A","B","C","D"],"answerIndex":0,"explanation":""},
                  {"conceptId":99,"stem":"概念不存在","options":["A","B","C","D"],"answerIndex":0,"explanation":"x"}
                ]}
                """;

        assertThat(generator.parse(json, CONCEPTS, Set.of())).isEmpty();
    }

    @Test
    @DisplayName("历史题干与本次重复的题都跳过")
    void skipsRepeatedStem() {
        String stem = "索引的最左前缀是什么？";
        String json = """
                {"items":[{"conceptId":1,"stem":"%s","options":["A","B","C","D"],
                  "answerIndex":0,"explanation":"x"}]}
                """.formatted(stem);

        assertThat(generator.parse(json, CONCEPTS, Set.of(ReviewQuestionGenerator.fingerprint(stem))))
                .isEmpty();
    }

    @Test
    @DisplayName("坏 JSON 返回空，不抛异常")
    void badJsonReturnsEmpty() {
        assertThat(generator.parse("{ items: ", CONCEPTS, Set.of())).isEmpty();
        assertThat(generator.parse("", CONCEPTS, Set.of())).isEmpty();
    }
}