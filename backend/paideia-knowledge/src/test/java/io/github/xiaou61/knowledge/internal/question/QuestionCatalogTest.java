package io.github.xiaou61.knowledge.internal.question;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuestionCatalogTest {

    private static final String BODY = """
            正文

            ```quiz
            {
              "id": "py-09-quiz",
              "title": "异常三问",
              "questions": [
                {"question": "int(\\"abc\\") 抛什么？", "options": ["ValueError", "TypeError"], "answer": 0},
                {"question": "finally 什么时候执行？", "options": ["总是", "仅正常"], "answer": 0}
              ]
            }
            ```
            """;

    @Test
    @DisplayName("解析出题目与稳定身份")
    void parsesQuestions() {
        List<QuestionCatalog.Question> questions = QuestionCatalog.parse(42, BODY);

        assertThat(questions).hasSize(2);
        assertThat(questions.get(0).questionKey()).isEqualTo("py-09-quiz#0");
        assertThat(questions.get(0).answerIndex()).isZero();
        assertThat(questions.get(0).fingerprint()).hasSize(64);
    }

    @Test
    @DisplayName("题干一变指纹就变，旧统计不会套到新题上")
    void fingerprintChangesWithStem() {
        var oldQuestion = QuestionCatalog.parse(42, BODY).get(0);
        var newQuestion = QuestionCatalog.parse(42, BODY.replace("抛什么？", "会抛什么？")).get(0);

        assertThat(newQuestion.fingerprint()).isNotEqualTo(oldQuestion.fingerprint());
        assertThat(newQuestion.questionKey()).isEqualTo(oldQuestion.questionKey());
    }

    @Test
    @DisplayName("缺少 id、坏 JSON、选项不足的题被跳过而不抛异常")
    void skipsBrokenBlocks() {
        String body = """
                ```quiz
                {"title":"没有 id","questions":[{"question":"q","options":["a","b"],"answer":0}]}
                ```

                ```quiz
                { broken json
                ```

                ```quiz
                {"id":"x","questions":[{"question":"只有一项","options":["a"],"answer":0}]}
                ```
                """;

        assertThat(QuestionCatalog.parse(1, body)).isEmpty();
    }

    @Test
    @DisplayName("按身份找题；找不到返回空")
    void findByIdentity() {
        assertThat(QuestionCatalog.find(42, BODY, "py-09-quiz", "py-09-quiz#1")).isPresent();
        assertThat(QuestionCatalog.find(42, BODY, "py-09-quiz", "py-09-quiz#9")).isEmpty();
    }
}
