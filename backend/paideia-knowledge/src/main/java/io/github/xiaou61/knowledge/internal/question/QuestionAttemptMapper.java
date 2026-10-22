package io.github.xiaou61.knowledge.internal.question;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * WORK-033 的四张表：作答、题目关联、掌握状态、题目难度。
 *
 * <p>掌握状态与难度都用 `ON DUPLICATE KEY UPDATE`：它们是「当前值」而不是账本，
 * 逐条历史在 `question_attempts` 里，重算随时可以从那里重放。
 */
@Mapper
public interface QuestionAttemptMapper {

    @Insert("""
            INSERT INTO question_attempts
                (user_id, entry_id, block_key, question_key, fingerprint, choice_index,
                 correct, latency_ms, judged_by, created_at)
            VALUES (#{userId}, #{entryId}, #{blockKey}, #{questionKey}, #{fingerprint},
                    #{choiceIndex}, #{correct}, #{latencyMs}, #{judgedBy}, #{now})
            """)
    int insertAttempt(@Param("userId") long userId, @Param("entryId") long entryId,
                      @Param("blockKey") String blockKey, @Param("questionKey") String questionKey,
                      @Param("fingerprint") String fingerprint,
                      @Param("choiceIndex") Integer choiceIndex, @Param("correct") boolean correct,
                      @Param("latencyMs") Integer latencyMs, @Param("judgedBy") String judgedBy,
                      @Param("now") Instant now);

    @Select("""
            SELECT concept_id, weight FROM question_concepts
            WHERE entry_id = #{entryId} AND block_key = #{blockKey} AND question_key = #{questionKey}
            ORDER BY id
            """)
    List<LinkRow> findLinks(@Param("entryId") long entryId, @Param("blockKey") String blockKey,
                            @Param("questionKey") String questionKey);

    @Delete("""
            DELETE FROM question_concepts
            WHERE entry_id = #{entryId} AND block_key = #{blockKey} AND question_key = #{questionKey}
            """)
    int deleteLinks(@Param("entryId") long entryId, @Param("blockKey") String blockKey,
                    @Param("questionKey") String questionKey);

    @Insert("""
            INSERT INTO question_concepts
                (entry_id, block_key, question_key, concept_id, weight, source, confidence, created_at)
            VALUES (#{entryId}, #{blockKey}, #{questionKey}, #{conceptId}, #{weight},
                    #{source}, #{confidence}, #{now})
            """)
    int insertLink(@Param("entryId") long entryId, @Param("blockKey") String blockKey,
                   @Param("questionKey") String questionKey, @Param("conceptId") long conceptId,
                   @Param("weight") double weight, @Param("source") String source,
                   @Param("confidence") Double confidence, @Param("now") Instant now);

    @Select("""
            SELECT user_id, concept_id, mastery, attempts, correct FROM concept_mastery_state
            WHERE user_id = #{userId}
            """)
    List<MasteryRow> findMastery(@Param("userId") long userId);

    @Select("""
            SELECT user_id, concept_id, mastery, attempts, correct FROM concept_mastery_state
            WHERE user_id = #{userId} AND concept_id = #{conceptId}
            """)
    Optional<MasteryRow> findMasteryOf(@Param("userId") long userId, @Param("conceptId") long conceptId);

    @Insert("""
            INSERT INTO concept_mastery_state
                (user_id, concept_id, mastery, attempts, correct, updated_at)
            VALUES (#{userId}, #{conceptId}, #{mastery}, #{attempts}, #{correct}, #{now})
            ON DUPLICATE KEY UPDATE
                mastery = VALUES(mastery), attempts = VALUES(attempts),
                correct = VALUES(correct), updated_at = VALUES(updated_at)
            """)
    int upsertMastery(@Param("userId") long userId, @Param("conceptId") long conceptId,
                      @Param("mastery") double mastery, @Param("attempts") int attempts,
                      @Param("correct") int correct, @Param("now") Instant now);

    @Select("""
            SELECT fingerprint, entry_id, block_key, question_key, rating, attempts, correct
            FROM question_difficulty WHERE fingerprint = #{fingerprint}
            """)
    Optional<RatingRow> findRating(@Param("fingerprint") String fingerprint);

    @Insert("""
            INSERT INTO question_difficulty
                (fingerprint, entry_id, block_key, question_key, rating, attempts, correct, updated_at)
            VALUES (#{fingerprint}, #{entryId}, #{blockKey}, #{questionKey}, #{rating},
                    #{attempts}, #{correct}, #{now})
            ON DUPLICATE KEY UPDATE
                rating = VALUES(rating), attempts = VALUES(attempts),
                correct = VALUES(correct), updated_at = VALUES(updated_at)
            """)
    int upsertRating(@Param("fingerprint") String fingerprint, @Param("entryId") long entryId,
                     @Param("blockKey") String blockKey, @Param("questionKey") String questionKey,
                     @Param("rating") double rating, @Param("attempts") int attempts,
                     @Param("correct") int correct, @Param("now") Instant now);

    @Select("""
            SELECT from_concept_id, to_concept_id FROM knowledge_concept_edges
            """)
    List<EdgeRow> findEdges();

    @Select("""
            SELECT entry_id, concept_id FROM knowledge_entry_concepts WHERE entry_id = #{entryId}
            """)
    List<EntryConceptRow> findEntryConcepts(@Param("entryId") long entryId);

    @Select("""
            SELECT id, name, summary FROM knowledge_concepts
            """)
    List<ConceptRow> findAllConcepts();

    record LinkRow(long conceptId, double weight) {
    }

    record MasteryRow(long userId, long conceptId, double mastery, int attempts, int correct) {
    }

    record RatingRow(String fingerprint, long entryId, String blockKey, String questionKey,
                     double rating, int attempts, int correct) {
    }

    record EdgeRow(long fromConceptId, long toConceptId) {
    }

    record EntryConceptRow(long entryId, long conceptId) {
    }

    record ConceptRow(long id, String name, String summary) {
    }
}
