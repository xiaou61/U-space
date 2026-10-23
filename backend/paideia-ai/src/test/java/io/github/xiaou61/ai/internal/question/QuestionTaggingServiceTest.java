package io.github.xiaou61.ai.internal.question;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xiaou61.knowledge.QuestionTaggingPort.Concept;
import io.github.xiaou61.knowledge.QuestionTaggingPort.Question;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 只测解析与过滤：模型调用本身需要上游，不放进单元测试。 */
class QuestionTaggingServiceTest {

    private final QuestionTaggingService service = new QuestionTaggingService(null, null, null);

    private static final List<Question> QUESTIONS = List.of(
            new Question("quiz#0", "索引的最左前缀是什么", List.of("索引", "事务")),
            new Question("quiz#1", "事务的隔离级别", List.of("读已提交", "可重复读")));

    private static final List<Concept> CONCEPTS = List.of(
            new Concept(1, "索引", null),
            new Concept(2, "事务", null));

    @Test
    @DisplayName("保留输入内的 id，丢弃模型编造的题目与概念")
    void filtersUnknownIds() {
        String json = """
                {"items":[
                  {"questionKey":"quiz#0","conceptIds":[1,99]},
                  {"questionKey":"编造的题","conceptIds":[2]}
                ]}
                """;

        var result = service.parse(json, QUESTIONS, CONCEPTS, "deepseek");

        assertThat(result.suggestions()).hasSize(1);
        assertThat(result.suggestions().get(0).questionKey()).isEqualTo("quiz#0");
        assertThat(result.suggestions().get(0).conceptId()).isEqualTo(1);
    }

    @Test
    @DisplayName("重复的 (题目, 概念) 只留一条")
    void dedupesPairs() {
        String json = """
                {"items":[{"questionKey":"quiz#1","conceptIds":[2,2]}]}
                """;

        assertThat(service.parse(json, QUESTIONS, CONCEPTS, "deepseek").suggestions()).hasSize(1);
    }

    @Test
    @DisplayName("空内容或坏 JSON 返回空，不抛异常")
    void badInputReturnsEmpty() {
        assertThat(service.parse("", QUESTIONS, CONCEPTS, "m").suggestions()).isEmpty();
        assertThat(service.parse("抱歉，我无法完成。", QUESTIONS, CONCEPTS, "m").suggestions()).isEmpty();
        assertThat(service.parse("{ items: ", QUESTIONS, CONCEPTS, "m").suggestions()).isEmpty();
    }
}