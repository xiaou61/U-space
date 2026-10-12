package io.github.xiaou61.ai.internal.memory;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 记忆两路的读写。
 *
 * <p>**归属写在 SQL 里**，与 {@code EntrySummaryMapper}、{@code ConversationMapper} 同一纪律：
 * 记忆是严格私有的（用户裁决 3），漏写一处 {@code user_id} 就等于把别人的记忆读出去。
 *
 * <p>全文那路的检索按 {@code SearchQuery} 判定走 `MATCH … AGAINST`（≥ ngram 分词长度）还是
 * `LIKE`（更短，否则**永远召回为零**）。两条路都用参数绑定；`LIKE` 的通配符转义在
 * {@code SearchQuery.likePattern} 里做。
 */
@Mapper
interface MemoryMapper {

    @Insert("""
            INSERT INTO ai_memories_structured (user_id, kind, ref_id, content)
            VALUES (#{userId}, #{kind}, #{refId}, #{content})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertStructured(MemoryRow memory);

    @Insert("""
            INSERT INTO ai_memories_text (user_id, content)
            VALUES (#{userId}, #{content})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertText(MemoryRow memory);

    /** 结构化记忆按**最近**取：它们都是"关于这个学习者的事实"，时间新旧就是最自然的取舍。 */
    @Select("""
            SELECT id, user_id, kind, ref_id, content, created_at
            FROM ai_memories_structured
            WHERE user_id = #{userId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<MemoryRow> selectStructuredRecent(@Param("userId") long userId, @Param("limit") int limit);

    @Select("""
            SELECT id, user_id, content, created_at
            FROM ai_memories_text
            WHERE user_id = #{userId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<MemoryRow> selectTextRecent(@Param("userId") long userId, @Param("limit") int limit);

    @Select("""
            <script>
            SELECT id, user_id, content, created_at
            FROM ai_memories_text
            WHERE user_id = #{userId}
              <choose>
                <when test="fulltext">AND MATCH(content) AGAINST(#{keyword})</when>
                <otherwise>AND content LIKE #{likePattern}</otherwise>
              </choose>
            ORDER BY id DESC
            LIMIT #{limit}
            </script>
            """)
    List<MemoryRow> searchText(@Param("userId") long userId,
                               @Param("keyword") String keyword,
                               @Param("likePattern") String likePattern,
                               @Param("fulltext") boolean fulltext,
                               @Param("limit") int limit);

    /**
     * 逐条删除（用户裁决 13）。
     *
     * <p>两路各一条语句：id 在两张表里各自独立，所以调用方必须说明删的是哪一路。
     * 归属条件同样在 SQL 里——否则删别人的记忆只是"传个 id"的事。
     */
    @Delete("DELETE FROM ai_memories_structured WHERE id = #{id} AND user_id = #{userId}")
    int deleteStructured(@Param("id") long id, @Param("userId") long userId);

    @Delete("DELETE FROM ai_memories_text WHERE id = #{id} AND user_id = #{userId}")
    int deleteText(@Param("id") long id, @Param("userId") long userId);
}
