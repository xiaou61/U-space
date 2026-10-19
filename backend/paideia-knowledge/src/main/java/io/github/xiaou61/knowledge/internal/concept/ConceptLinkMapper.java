package io.github.xiaou61.knowledge.internal.concept;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 条目↔概念的关联与概念间的边（WORK-021）。
 *
 * <p>两张表在这里合起来读：图谱聚合需要它们同时到位，分两个 mapper 只是把一次查询拆成两半。
 * 插入一律"撞唯一键即成功"——重复建立同一条关联/边不是错误。
 */
@Mapper
public interface ConceptLinkMapper {

    @Select("""
            SELECT entry_id, concept_id FROM knowledge_entry_concepts
            """)
    List<ConceptLinks.EntryLink> findAllEntryLinks();

    @Select("""
            SELECT id, from_concept_id, to_concept_id, relation_type FROM knowledge_concept_edges
            """)
    List<ConceptLinks.Edge> findAllEdges();

    /**
     * 关联条目到概念。
     *
     * <p>{@code INSERT IGNORE} 而不是 {@code ON DUPLICATE KEY UPDATE}：这张表只有主键没有
     * 可更新的列，重复关联的语义就是"已经是了"。
     */
    @Insert("""
            INSERT IGNORE INTO knowledge_entry_concepts (entry_id, concept_id, created_at)
            VALUES (#{entryId}, #{conceptId}, #{now})
            """)
    int linkEntry(@Param("entryId") Long entryId, @Param("conceptId") Long conceptId,
                  @Param("now") Instant now);

    @Delete("""
            DELETE FROM knowledge_entry_concepts
            WHERE entry_id = #{entryId} AND concept_id = #{conceptId}
            """)
    int unlinkEntry(@Param("entryId") Long entryId, @Param("conceptId") Long conceptId);

    /** 重复建立同一条边也算成功（唯一键吞掉）。 */
    @Insert("""
            INSERT IGNORE INTO knowledge_concept_edges
                (from_concept_id, to_concept_id, relation_type, created_at)
            VALUES (#{fromConceptId}, #{toConceptId}, #{relationType}, #{now})
            """)
    int insertEdge(@Param("fromConceptId") Long fromConceptId,
                   @Param("toConceptId") Long toConceptId,
                   @Param("relationType") String relationType,
                   @Param("now") Instant now);

    /**
     * 建边后取回 id。重复插入时 {@code INSERT IGNORE} 返回 0，这里按三元组查一次，
     * 管理端据此拿到稳定的 id 而非"没拿到"。
     */
    @Select("""
            SELECT id FROM knowledge_concept_edges
            WHERE from_concept_id = #{fromConceptId}
              AND to_concept_id = #{toConceptId}
              AND relation_type = #{relationType}
            """)
    Long findEdgeId(@Param("fromConceptId") Long fromConceptId,
                    @Param("toConceptId") Long toConceptId,
                    @Param("relationType") String relationType);

    @Delete("DELETE FROM knowledge_concept_edges WHERE id = #{id}")
    int deleteEdge(@Param("id") Long id);
}
