package io.github.xiaou61.knowledge.internal.entry;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 浏览记录访问（WORK-021）。
 *
 * <p><b>每条查询都带 {@code user_id}</b>，与自评、学习记录同一纪律：隔离靠数据过滤，
 * 不靠调用方记得过滤。入口的 user_id 只来自令牌。
 */
@Mapper
public interface EntryViewMapper {

    /** 某条目的一行浏览记录。 */
    record Row(long entryId, int viewCount, Instant lastViewedAt) {
    }

    /**
     * 记一次浏览。
     *
     * <p>用 upsert 而不是"先查再插"：并发打开同一篇会撞唯一键。累加发生在数据库里，
     * 两个并发请求都不会丢计数。
     */
    @Insert("""
            INSERT INTO knowledge_entry_views
                (user_id, entry_id, view_count, first_viewed_at, last_viewed_at)
            VALUES (#{userId}, #{entryId}, 1, #{now}, #{now}) AS incoming
            ON DUPLICATE KEY UPDATE
                view_count = knowledge_entry_views.view_count + 1,
                last_viewed_at = incoming.last_viewed_at
            """)
    int upsert(@Param("userId") Long userId, @Param("entryId") Long entryId, @Param("now") Instant now);

    @Select("""
            SELECT entry_id, view_count, last_viewed_at
            FROM knowledge_entry_views
            WHERE user_id = #{userId}
            """)
    List<Row> findByUser(@Param("userId") Long userId);

    @Select("""
            SELECT entry_id, view_count, last_viewed_at
            FROM knowledge_entry_views
            WHERE user_id = #{userId} AND entry_id = #{entryId}
            """)
    Optional<Row> findOne(@Param("userId") Long userId, @Param("entryId") Long entryId);
}
