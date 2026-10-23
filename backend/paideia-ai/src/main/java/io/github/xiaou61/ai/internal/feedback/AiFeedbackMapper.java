package io.github.xiaou61.ai.internal.feedback;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 反馈的读写（WORK-036 C 组）。
 *
 * <p>**写入是 upsert**：唯一键 {@code (user_id, trace_id)} 让"同一用户对同一轮改判"变成覆盖，
 * 而不是在列表里堆出两条互相矛盾的记录。`trace_id` 为 NULL 时唯一键不生效（MySQL 语义），
 * 那种情况本来就没有轮次可挂。
 */
@Mapper
interface AiFeedbackMapper {

    @Insert("""
            INSERT INTO ai_feedback
              (user_id, conversation_id, message_id, trace_id, kind, rating, reason, created_at)
            VALUES
              (#{userId}, #{conversationId}, #{messageId}, #{traceId}, #{kind}, #{rating}, #{reason}, #{createdAt})
            ON DUPLICATE KEY UPDATE
              rating = VALUES(rating), reason = VALUES(reason), created_at = VALUES(created_at),
              conversation_id = VALUES(conversation_id), message_id = VALUES(message_id),
              kind = VALUES(kind)
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int upsert(AiFeedbackRow row);

    @Select("""
            <script>
            SELECT id, user_id, conversation_id, message_id, trace_id, kind, rating, reason, created_at
            FROM ai_feedback
            WHERE created_at &gt;= #{from} AND created_at &lt; #{to}
            <if test="rating != null">AND rating = #{rating}</if>
            <if test="reason != null">AND reason = #{reason}</if>
            <if test="kind != null">AND kind = #{kind}</if>
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit} OFFSET #{offset}
            </script>
            """)
    List<AiFeedbackRow> page(@Param("from") Instant from, @Param("to") Instant to,
                             @Param("rating") String rating, @Param("reason") String reason,
                             @Param("kind") String kind,
                             @Param("limit") int limit, @Param("offset") long offset);

    @Select("""
            <script>
            SELECT COUNT(*) FROM ai_feedback
            WHERE created_at &gt;= #{from} AND created_at &lt; #{to}
            <if test="rating != null">AND rating = #{rating}</if>
            <if test="reason != null">AND reason = #{reason}</if>
            <if test="kind != null">AND kind = #{kind}</if>
            </script>
            """)
    long count(@Param("from") Instant from, @Param("to") Instant to,
               @Param("rating") String rating, @Param("reason") String reason,
               @Param("kind") String kind);

    /** 概览用：按评价的计数。 */
    @Select("""
            SELECT rating AS name, COUNT(*) AS count FROM ai_feedback
            WHERE created_at >= #{from} AND created_at < #{to}
            GROUP BY rating
            """)
    List<CountBy> countByRating(@Param("from") Instant from, @Param("to") Instant to);

    record CountBy(String name, long count) {
    }
}
