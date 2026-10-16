package io.github.xiaou61.ai.internal.settings;

import java.time.Instant;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户 AI 设置的读写。
 *
 * <p>**没有"读别人的设置"这条路径**：归属就是主键 `user_id`，查询与写入都只能针对本人。
 *
 * <p>写入用一条 `INSERT … ON DUPLICATE KEY UPDATE`（upsert）而不是"先查再插或改"：
 * 后者的两步之间会有并发窗口，而设置是**整行覆盖**的语义，一条语句既原子又不需要事务包装。
 *
 * <p>{@code updated_at} 由应用传入（按 UTC），不用库的 `CURRENT_TIMESTAMP`——库会话时区是
 * +08:00，默认值会让同一列出现两种时区（V6 的约定，2026-10-12 踩过）。
 */
@Mapper
interface AiUserSettingsMapper {

    @Select("""
            SELECT user_id, memory_enabled, reference_saved_memories, reference_chat_history, updated_at
            FROM ai_user_settings
            WHERE user_id = #{userId}
            """)
    Optional<AiUserSettings> find(@Param("userId") long userId);

    @Insert("""
            INSERT INTO ai_user_settings
                (user_id, memory_enabled, reference_saved_memories, reference_chat_history, updated_at)
            VALUES
                (#{userId}, #{memoryEnabled}, #{referenceSavedMemories}, #{referenceChatHistory}, #{updatedAt})
            ON DUPLICATE KEY UPDATE
                memory_enabled = VALUES(memory_enabled),
                reference_saved_memories = VALUES(reference_saved_memories),
                reference_chat_history = VALUES(reference_chat_history),
                updated_at = VALUES(updated_at)
            """)
    int upsert(@Param("userId") long userId,
               @Param("memoryEnabled") boolean memoryEnabled,
               @Param("referenceSavedMemories") boolean referenceSavedMemories,
               @Param("referenceChatHistory") boolean referenceChatHistory,
               @Param("updatedAt") Instant updatedAt);
}
