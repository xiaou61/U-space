package io.github.xiaou61.ai.internal.conversation;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 会话的读写。
 *
 * <p><b>归属写在 SQL 里</b>，与 {@code EntrySummaryMapper} 同一纪律：查询条件里带
 * {@code user_id}，漏写一处就等于把别人的会话读出去——写在 WHERE 里就不存在"忘了判断"这条路径。
 *
 * <p><b>过期也用 SQL 判</b>（{@code expires_at > now}），不查出来再在 Java 里比：
 * 这样"过期会话"在调用方看来就是"不存在"，与分享失效同一套语义，也不需要定时清理任务。
 * {@code now} 由调用方传入（应用按 UTC 算），不用库的 {@code NOW()}——库会话时区是 +08:00。
 */
@Mapper
interface ConversationMapper {

    @Insert("""
            INSERT INTO ai_conversations (user_id, entry_id, expires_at)
            VALUES (#{userId}, #{entryId}, #{expiresAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Conversation conversation);

    @Select("""
            SELECT id, user_id, entry_id, expires_at, created_at, updated_at
            FROM ai_conversations
            WHERE id = #{id} AND user_id = #{userId} AND expires_at > #{now}
            """)
    Optional<Conversation> findActiveByIdAndUser(@Param("id") long id,
                                                 @Param("userId") long userId,
                                                 @Param("now") Instant now);

    @Select("""
            SELECT id, user_id, entry_id, expires_at, created_at, updated_at
            FROM ai_conversations
            WHERE user_id = #{userId} AND expires_at > #{now}
            ORDER BY updated_at DESC, id DESC
            """)
    List<Conversation> listActiveByUser(@Param("userId") long userId, @Param("now") Instant now);

    /**
     * 把过期时间往后推（滑动过期）。
     *
     * <p>带上 {@code expires_at > now} 这个条件：已过期的会话不该被一条新消息"复活"——
     * 那会让过期时间形同虚设，也会让"过期即视为不存在"这条语义出现例外。
     *
     * @return 影响行数；0 表示会话不存在、不属于该用户或已过期
     */
    @Update("""
            UPDATE ai_conversations
            SET expires_at = #{expiresAt}
            WHERE id = #{id} AND user_id = #{userId} AND expires_at > #{now}
            """)
    int slideExpiry(@Param("id") long id,
                    @Param("userId") long userId,
                    @Param("expiresAt") Instant expiresAt,
                    @Param("now") Instant now);

    @Delete("DELETE FROM ai_conversations WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUser(@Param("id") long id, @Param("userId") long userId);
}
