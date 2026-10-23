package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.xiaou61.security.AuthPort;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** 到期复习列表（WORK-034 S1）：只返回到期项、按到期日升序、带概念名。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "PAIDEIA_TEST_DB_PASSWORD", matches = ".+")
class LearningReviewIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    AuthPort authPort;

    private final long subject = 334_455L;
    private long firstEntry;
    private long secondEntry;
    private long thirdEntry;
    private long conceptId;
    private String conceptName;

    @BeforeEach
    void setUp() {
        List<Long> entries = jdbcTemplate.queryForList(
                "SELECT id FROM knowledge_entries WHERE status = 'published' ORDER BY id LIMIT 3", Long.class);
        firstEntry = entries.get(0);
        secondEntry = entries.get(1);
        thirdEntry = entries.get(2);

        insertProgress(firstEntry, "-2 HOUR");
        insertProgress(secondEntry, "-1 HOUR");
        insertProgress(thirdEntry, "+1 DAY");

        conceptName = "复习概念-" + System.nanoTime();
        jdbcTemplate.update("""
                INSERT INTO knowledge_concepts (name, summary, created_at, updated_at)
                VALUES (?, '复习列表测试', UTC_TIMESTAMP(3), UTC_TIMESTAMP(3))
                """, conceptName);
        conceptId = jdbcTemplate.queryForObject(
                "SELECT id FROM knowledge_concepts WHERE name = ?", Long.class, conceptName);
        jdbcTemplate.update("""
                INSERT INTO knowledge_entry_concepts (entry_id, concept_id, created_at)
                VALUES (?, ?, UTC_TIMESTAMP(3))
                """, secondEntry, conceptId);
    }

    private void insertProgress(long entryId, String offset) {
        jdbcTemplate.update("""
                INSERT INTO learning_progress
                    (user_id, entry_id, challenges_json, quizzes_json, status, review_stage, review_due_at)
                VALUES (?, ?, '{}', '{}', 'passed', 2, DATE_ADD(UTC_TIMESTAMP(3), INTERVAL %s))
                """.formatted(offset), subject, entryId);
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM learning_progress WHERE user_id = ?", subject);
        jdbcTemplate.update("DELETE FROM knowledge_entry_concepts WHERE concept_id = ?", conceptId);
        jdbcTemplate.update("DELETE FROM knowledge_concepts WHERE id = ?", conceptId);
    }

    private String token() {
        return "Bearer " + authPort.issue(
                new AuthPort.Subject(String.valueOf(subject), AuthPort.ROLE_LEARNER)).value();
    }

    @Test
    @DisplayName("只返回到期项，并按到期日升序")
    void dueReviewsAreOrderedByDueDate() throws Exception {
        mockMvc.perform(get("/api/v1/learning/reviews").header("Authorization", token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(2))
                .andExpect(jsonPath("$.data.items[0].entryId").value(firstEntry))
                .andExpect(jsonPath("$.data.items[1].entryId").value(secondEntry));
    }

    @Test
    @DisplayName("条目挂了概念时带出概念名")
    void dueReviewsIncludeConceptName() throws Exception {
        mockMvc.perform(get("/api/v1/learning/reviews").header("Authorization", token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[1].conceptId").value(conceptId))
                .andExpect(jsonPath("$.data.items[1].conceptName").value(conceptName));
    }
}