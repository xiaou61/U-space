package io.github.xiaou61.ai.internal.summary;

import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 精简结果的读写。
 *
 * <p><b>可见性写在 SQL 里</b>而不是"查出来再在 Java 里判断"：漏改一处的后果可能是把别人的
 * 私有结果读出去，而写在 WHERE 里它和查询是同一件事，不存在"忘了判断"这条路径。
 *
 * <p><b>读过滤不含模型名</b>（写入身份里含）。这是实施时发现的一处设计矛盾：把模型名也拿来
 * 过滤，等于要求"读的人必须先有一个模型名"，而没配凭据的用户恰恰没有——那样他们就永远读不到
 * 任何公开结果，与需求"未配凭据者可读公开结果"直接冲突。模型名留在唯一键里足矣：
 * 它保证同一个人换模型再生成时不会被旧行顶掉。
 *
 * <p>取最新一条（{@code created_at DESC, id DESC}）：同一条内容有多个可见版本时，
 * 消费者看到最近生成的那份，而不是随机一份。
 */
@Mapper
interface EntrySummaryMapper {

    @Select("""
            SELECT id, entry_id, content_hash, model, prompt_version, owner_user_id, visibility, card_json, created_at
            FROM ai_entry_summaries
            WHERE entry_id = #{entryId}
              AND content_hash = #{contentHash}
              AND prompt_version = #{promptVersion}
              AND (visibility = 'public' OR owner_user_id = #{userId})
            ORDER BY created_at DESC, id DESC
            LIMIT 1
            """)
    Optional<EntrySummary> findVisible(@Param("entryId") long entryId,
                                       @Param("contentHash") String contentHash,
                                       @Param("promptVersion") String promptVersion,
                                       @Param("userId") long userId);

    @Insert("""
            INSERT INTO ai_entry_summaries
                (entry_id, content_hash, model, prompt_version, owner_user_id, visibility, card_json)
            VALUES
                (#{entryId}, #{contentHash}, #{model}, #{promptVersion}, #{ownerUserId}, #{visibility}, #{cardJson})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EntrySummary summary);
}
