package io.github.xiaou61.ai.internal.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xiaou61.knowledge.ReviewQuestionPort.Concept;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 复习题金标（WORK-034 TASK-009）。
 *
 * <p>用录制样本跑结构校验，不连上游：断言「不重复、恰好 4 个选项、答案下标合法、
 * 解析非空」。真连上游的结果单独记录在 testing/ 报告里。
 */
class ReviewGoldenTest {

    private static final List<Concept> CONCEPTS = List.of(
            new Concept(1, "索引", "B+ 树", "MySQL 索引"),
            new Concept(2, "事务", "ACID", "事务与锁"));

    @Test
    @DisplayName("20 条录制样本全部通过结构校验且不重复")
    void recordedSamplesAreStructurallyValid() throws Exception {
        String raw;
        try (InputStream in = getClass().getResourceAsStream("/review-golden/recorded.json")) {
            raw = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
        ReviewQuestionGenerator generator = new ReviewQuestionGenerator(null, null, null);

        var questions = generator.parse(new ObjectMapper().readTree(raw).toString(), CONCEPTS, Set.of());

        assertThat(questions).hasSize(20);
        Set<String> fingerprints = new HashSet<>();
        for (var question : questions) {
            assertThat(question.options()).hasSize(4);
            assertThat(question.answerIndex()).isBetween(0, 3);
            assertThat(question.explanation()).isNotBlank();
            assertThat(fingerprints.add(question.fingerprint())).isTrue();
        }
    }
}
