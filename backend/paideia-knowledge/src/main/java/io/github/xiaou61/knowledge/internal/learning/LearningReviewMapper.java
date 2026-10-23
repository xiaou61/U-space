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

    record DueRow(long entryId, String title, Instant reviewDueAt, int reviewStage) {
    }

    record ConceptRow(long entryId, long conceptId, String conceptName) {
    }
}