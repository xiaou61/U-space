package io.github.xiaou61.ai.internal.conversation;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 消息的读写。
 *
 * <p><b>取最近 N 条按 id 排</b>而不是 {@code created_at}：id 单调递增，同毫秒写入的并列消息也有
 * 确定顺序；时间列只用于展示。查询里 {@code ORDER BY id DESC LIMIT} 取的是最近的，返回前在
 * Java 里翻成"旧 → 新"（模型要求的顺序）。
 *
 * <p><b>条数上限用"删最旧的若干条"实现</b>（{@code DELETE ... ORDER BY id ASC LIMIT}），
 * 而不是靠后台任务：线上没有定时任务基设，写入路径顺手裁掉最省，也不会让单条会话无限增长。
 * 裁多少条由 {@link ConversationWindow#messagesToDrop} 这个纯函数算，便于单测。
 */
@Mapper
interface ConversationMessageMapper {

    @Insert("""
            INSERT INTO ai_messages (conversation_id, role, content, token_usage)
            VALUES (#{conversationId}, #{role}, #{content}, #{tokenUsageJson})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ConversationMessage message);

    /**
     * 最近 {@code limit} 条，**新 → 旧**；调用方负责反过来。
     *
     * <p>{@code token_usage} 要**显式起别名**：列的 `token_usage` 与字段的 `tokenUsageJson` 不是
     * "下划线转驼峰"能对上的关系（`tokenUsage` ≠ `tokenUsageJson`），自动映射会**静默给 null**。
     * 2026-10-16 由 WORK-011 的读历史用例抓出来——在那之前这列只被写过、没被读过，所以一直潜伏。
     */
    @Select("""
            SELECT id, conversation_id, role, content, token_usage AS tokenUsageJson, created_at
            FROM ai_messages
            WHERE conversation_id = #{conversationId}
            ORDER BY id DESC
            LIMIT #{limit}
            """)
    List<ConversationMessage> selectRecentNewestFirst(@Param("conversationId") long conversationId,
                                                      @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM ai_messages WHERE conversation_id = #{conversationId}")
    long countByConversation(@Param("conversationId") long conversationId);

    /**
     * 某条消息**之后**的全部消息（旧 → 新）。自动整理的"增量"就是它（WORK-013）。
     *
     * <p>按 id 而不是时间：id 单调递增，同毫秒写入也不会乱序（与 {@code selectRecentNewestFirst} 同一约定）。
     * `afterId` 为 null 时取全部——第一次整理时游标就是 null。
     */
    @Select("""
            <script>
            SELECT id, conversation_id, role, content, token_usage AS tokenUsageJson, created_at
            FROM ai_messages
            WHERE conversation_id = #{conversationId}
              <if test="afterId != null">AND id &gt; #{afterId}</if>
            ORDER BY id ASC
            LIMIT #{limit}
            </script>
            """)
    List<ConversationMessage> selectAfter(@Param("conversationId") long conversationId,
                                          @Param("afterId") Long afterId,
                                          @Param("limit") int limit);

    /**
     * 游标之后有多少条**助手**消息——即"攒了几轮"（WORK-013）。
     *
     * <p>数助手消息而不是用户消息：一轮可能只有用户那条（上游失败时助手消息不落库），
     * 那样的轮次没有可整理的内容，不该把它算进 N。
     */
    @Select("""
            <script>
            SELECT COUNT(*) FROM ai_messages
            WHERE conversation_id = #{conversationId} AND role = 'assistant'
              <if test="afterId != null">AND id &gt; #{afterId}</if>
            </script>
            """)
    long countAssistantAfter(@Param("conversationId") long conversationId,
                             @Param("afterId") Long afterId);

    /** 删掉最旧的 {@code dropCount} 条。{@code dropCount <= 0} 时调用方不该走到这里。 */
    @Delete("""
            DELETE FROM ai_messages
            WHERE conversation_id = #{conversationId}
            ORDER BY id ASC
            LIMIT #{dropCount}
            """)
    int deleteOldest(@Param("conversationId") long conversationId, @Param("dropCount") int dropCount);
}
