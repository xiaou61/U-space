package io.github.xiaou61.knowledge.internal.concept;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 概念表访问（WORK-021）。
 *
 * <p>概念是公共内容：这里没有 {@code user_id} 过滤，权限由端点上的 {@code @ApiAccess} 决定。
 * 全量读取（{@link #findAll()}）与 {@code CategoryMapper.findAll()} 同一取舍：概念数量级是
 * 数百，一次取全再在内存里算比逐条查便宜得多。
 */
@Mapper
public interface ConceptMapper {

    @Select("""
            SELECT id, name, summary, category_id, created_at, updated_at
            FROM knowledge_concepts
            ORDER BY id
            """)
    List<Concept> findAll();

    @Select("""
            SELECT id, name, summary, category_id, created_at, updated_at
            FROM knowledge_concepts WHERE id = #{id}
            """)
    Concept findById(@Param("id") Long id);

    /**
     * 名称查重。带 {@code excludeId} 是因为改名时会把"自己"算进去，
     * 不排除的话改回原名会被判成冲突。
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM knowledge_concepts WHERE name = #{name}
            <if test="excludeId != null">AND id &lt;&gt; #{excludeId}</if>
            </script>
            """)
    long countByName(@Param("name") String name, @Param("excludeId") Long excludeId);

    @Insert("""
            INSERT INTO knowledge_concepts (name, summary, category_id, created_at, updated_at)
            VALUES (#{name}, #{summary}, #{categoryId}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Concept concept);

    @Update("""
            UPDATE knowledge_concepts
            SET name = #{name}, summary = #{summary}, category_id = #{categoryId}, updated_at = #{updatedAt}
            WHERE id = #{id}
            """)
    int update(Concept concept);

    /** 关联与边由外键 ON DELETE CASCADE 一并删除，这里只删概念本身。 */
    @Delete("DELETE FROM knowledge_concepts WHERE id = #{id}")
    int delete(@Param("id") Long id);

    /** 某概念下的条目数，管理端列表用。 */
    @Select("""
            SELECT COUNT(*) FROM knowledge_entry_concepts WHERE concept_id = #{conceptId}
            """)
    long countEntryLinks(@Param("conceptId") Long conceptId);

    /** 与某概念相连的边数（进出都算），管理端列表用。 */
    @Select("""
            SELECT COUNT(*) FROM knowledge_concept_edges
            WHERE from_concept_id = #{conceptId} OR to_concept_id = #{conceptId}
            """)
    long countEdges(@Param("conceptId") Long conceptId);
}
