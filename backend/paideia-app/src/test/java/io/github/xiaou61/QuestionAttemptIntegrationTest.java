package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.xiaou61.security.AuthPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 逐题作答的集成测试（WORK-033）。
 *
 * <p>钉住三件事：服务端按正文判定（客户端不发 correct）、掌握度随作答上下移动、
 * 越界选择返回 400 且不落库。用自建条目而不是改种子，避免影响同库其它用例。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "PAIDEIA_TEST_DB_PASSWORD", matches = ".+")
class QuestionAttemptIntegrationTest {

    private static final String BLOCK = "it-quiz";
    private static final String QUIZ_BODY = """
            ```quiz
            {"id":"it-quiz","questions":[
              {"question":"两数相加用哪个运算符？","options":["+","-"],"answer":0}
            ]}
            ```
            """;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    AuthPort authPort;

    private long entryId;
    private long conceptId;
    private String entryTitle;
    private String conceptName;
    private final long subject = 333_333L;

    @BeforeEach
    void setUp() {
        entryTitle = "作答集成测试-" + System.nanoTime();
        conceptName = "作答概念-" + System.nanoTime();
        jdbcTemplate.update("""
                INSERT INTO knowledge_entries (title, body, status, published_at)
                VALUES (?, ?, 'published', UTC_TIMESTAMP(3))
                """, entryTitle, QUIZ_BODY);
        entryId = jdbcTemplate.queryForObject(
                "SELECT id FROM knowledge_entries WHERE title = ?", Long.class, entryTitle);
        jdbcTemplate.update("""
                INSERT INTO knowledge_concepts (name, summary, created_at, updated_at)
                VALUES (?, '集成测试概念', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3))
                """, conceptName);
        conceptId = jdbcTemplate.queryForObject(
                "SELECT id FROM knowledge_concepts WHERE name = ?", Long.class, conceptName);
        jdbcTemplate.update("""
                INSERT INTO question_concepts
                    (entry_id, block_key, question_key, concept_id, weight, source, created_at)
                VALUES (?, ?, ?, ?, 1.00, 'author', UTC_TIMESTAMP(3))
                """, entryId, BLOCK, BLOCK + "#0", conceptId);
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM question_attempts WHERE user_id = ?", subject);
        jdbcTemplate.update("DELETE FROM concept_mastery_state WHERE user_id = ?", subject);
        jdbcTemplate.update("DELETE FROM question_difficulty WHERE entry_id = ?", entryId);
        jdbcTemplate.update("DELETE FROM question_concepts WHERE entry_id = ?", entryId);
        jdbcTemplate.update("DELETE FROM knowledge_entries WHERE id = ?", entryId);
        jdbcTemplate.update("DELETE FROM knowledge_concepts WHERE id = ?", conceptId);
    }

    private String token() {
        return "Bearer " + authPort.issue(new AuthPort.Subject(String.valueOf(subject), AuthPort.ROLE_LEARNER)).value();
    }

    private String body(int choice) {
        return """
                {"entryId":%d,"blockKey":"%s","questionKey":"%s#0","choice":%d}
                """.formatted(entryId, BLOCK, BLOCK, choice);
    }

    @Test
    @DisplayName("答对：写入作答、掌握度高于先验；再答错时掌握度下降")
    void correctThenWrongMovesMastery() throws Exception {
        mockMvc.perform(post("/api/v1/learning/attempts")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(0)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correct").value(true))
                .andExpect(jsonPath("$.data.concepts[0].conceptId").value(conceptId));

        Double afterCorrect = jdbcTemplate.queryForObject("""
                SELECT mastery FROM concept_mastery_state WHERE user_id = ? AND concept_id = ?
                """, Double.class, subject, conceptId);
        assertThat(afterCorrect).isNotNull().isGreaterThan(0.3);

        mockMvc.perform(post("/api/v1/learning/attempts")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.correct").value(false));

        Double afterWrong = jdbcTemplate.queryForObject("""
                SELECT mastery FROM concept_mastery_state WHERE user_id = ? AND concept_id = ?
                """, Double.class, subject, conceptId);
        assertThat(afterWrong).isLessThan(afterCorrect);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM question_attempts WHERE user_id = ?", Integer.class, subject))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("选项越界返回 400，且不写作答")
    void outOfRangeChoiceIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/learning/attempts")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(9)))
                .andExpect(status().isBadRequest());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM question_attempts WHERE user_id = ?", Integer.class, subject))
                .isZero();
    }

    @Test
    @DisplayName("找不到题目返回 400，不静默记一条假作答")
    void unknownQuestionIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/learning/attempts")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"entryId":%d,"blockKey":"nope","questionKey":"nope#0","choice":0}
                                """.formatted(entryId)))
                .andExpect(status().isBadRequest());

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM question_attempts WHERE user_id = ?", Integer.class, subject))
                .isZero();
    }
}
