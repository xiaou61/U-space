package io.github.xiaou61.knowledge.internal.learning;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 到期复习的读取（WORK-034 S1）。只读，不改排期。 */
@Mapper
public interface LearningReviewMapper {

    @Select("""
            SELECT p.entry_id, e.title, p.review_due_at, p.review_stage
            FROM learning_progress p
            JOIN knowledge_entries e ON e.id = p.entry_id AND e.status = 'published'
            WHERE p.user_id = #{userId}
              AND p.review_due_at IS NOT NULL
              AND p.review_due_at <= #{now}
            ORDER BY p.review_due_at ASC, p.entry_id ASC
            """)
    List<DueRow> findDue(@Param("userId") long userId, @Param("now") Instant now);

    /** 条目 → 第一个概念名；没有关联时概念列为 null。 */
    @Select("""
            <script>
            SELECT ec.entry_id, c.id AS concept_id, c.name AS concept_name
            FROM knowledge_entry_concepts ec
            JOIN knowledge_concepts c ON c.id = ec.concept_id
            WHERE ec.entry_id IN
              <foreach item="id" collection="entryIds" open="(" separator="," close=")">#{id}</foreach>
            ORDER BY ec.entry_id, c.id
            </script>
            """)
    List<ConceptRow> findConceptNames(@Param("entryIds") List<Long> entryIds);

    // ---- S2 复习会话 ----

    @Select("""
            SELECT id, user_id, concept_ids_json, items_json, answers_json, expires_at, completed_at
            FROM review_sessions
            WHERE user_id = #{userId} AND completed_at IS NULL AND expires_at > #{now}
            ORDER BY id DESC LIMIT 1
            """)
    java.util.Optional<SessionRow> findOpen(@Param("userId") long userId, @Param("now") Instant now);

    @org.apache.ibatis.annotations.Insert("""
            INSERT INTO review_sessions
                (user_id, concept_ids_json, items_json, created_at, expires_at)
            VALUES (#{userId}, #{conceptsJson}, #{itemsJson}, #{now}, #{expiresAt})
            """)
    int insertSession(@Param("userId") long userId, @Param("conceptsJson") String conceptsJson,
                      @Param("itemsJson") String itemsJson, @Param("now") Instant now,
                      @Param("expiresAt") Instant expiresAt);

    @Select("SELECT LAST_INSERT_ID()")
    long lastInsertId();

    @Select("""
            SELECT id, user_id, concept_ids_json, items_json, answers_json, expires_at, completed_at
            FROM review_sessions WHERE id = #{id} AND user_id = #{userId}
            """)
    java.util.Optional<SessionRow> findSession(@Param("id") long id, @Param("userId") long userId);

    /** 幂等闸：只有第一次交卷能写入，返回 0 表示已被交过。 */
    @org.apache.ibatis.annotations.Update("""
            UPDATE review_sessions SET answers_json = #{answersJson}, completed_at = #{now}
            WHERE id = #{id} AND user_id = #{userId} AND completed_at IS NULL
            """)
    int completeSession(@Param("id") long id, @Param("userId") long userId,
                        @Param("answersJson") String answersJson, @Param("now") Instant now);

    @Select("""
            SELECT items_json FROM review_sessions
            WHERE user_id = #{userId} ORDER BY id DESC LIMIT #{limit}
            """)
    java.util.List<String> recentItems(@Param("userId") long userId, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT concept_id, name, summary FROM (
              SELECT id AS concept_id, name, summary FROM knowledge_concepts
              WHERE id IN
                <foreach item="id" collection="ids" open="(" separator="," close=")">#{id}</foreach>
            ) t
            </script>
            """)
    java.util.List<ConceptTextRow> findConceptTexts(@Param("ids") java.util.List<Long> ids);

    @Select("""
            SELECT review_stage FROM learning_progress WHERE user_id = #{userId} AND entry_id = #{entryId}
            """)
    java.util.Optional<Integer> progressStage(@Param("userId") long userId, @Param("entryId") long entryId);

    @org.apache.ibatis.annotations.Update("""
            UPDATE learning_progress SET review_stage = #{stage}, review_due_at = #{dueAt}
            WHERE user_id = #{userId} AND entry_id = #{entryId}
            """)
    int updateSchedule(@Param("userId") long userId, @Param("entryId") long entryId,
                       @Param("stage") int stage, @Param("dueAt") Instant dueAt);

    record SessionRow(long id, long userId, String conceptIdsJson, String itemsJson,
                      String answersJson, Instant expiresAt, Instant completedAt) {
    }

    record ConceptTextRow(long conceptId, String name, String summary) {
    }

    record DueRow(long entryId, String title, Instant reviewDueAt, int reviewStage) {
    }

    record ConceptRow(long entryId, long conceptId, String conceptName) {
    }
}