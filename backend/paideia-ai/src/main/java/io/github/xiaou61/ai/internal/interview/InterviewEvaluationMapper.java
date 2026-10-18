package io.github.xiaou61.ai.internal.interview;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xiaou61.ai.InterviewEvaluation;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 面试评估的读写（WORK-010 T-11）。
 *
 * <p>**归属写在 SQL 里**：凡是读用户数据的地方都带 `user_id`，漏写一处就等于把别人的评分读出去。
 *
 * <p>评分整体存一列 JSON，不拆成列：它是**一份快照**，将来维度名会变（模型换措辞），
 * 拆成列每年都要迁移。要按维度查询时再从 JSON 里取（MySQL 支持 JSON 路径查询）。
 */
@Mapper
interface InterviewEvaluationMapper {

    /**
     * 落库一份评估。
     *
     * <p>序列化失败**不吞**：说明 `InterviewEvaluation` 里有 Jackson 处理不了的东西，
     * 那是代码错误，应该在测试里就炸出来，而不是写一行空 JSON 进库。
     */
    @Insert("""
            INSERT INTO ai_interview_evaluations (user_id, conversation_id, score_json, created_at)
            VALUES (#{userId}, #{conversationId}, #{scoreJson}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRow(EvaluationRow row);

    @Select("""
            SELECT id, user_id, conversation_id, score_json, created_at
            FROM ai_interview_evaluations
            WHERE user_id = #{userId}
            ORDER BY created_at DESC, id DESC
            LIMIT #{limit}
            """)
    List<EvaluationRow> listRowsByUser(@Param("userId") long userId, @Param("limit") int limit);
    @org.apache.ibatis.annotations.Delete("DELETE FROM ai_interview_evaluations WHERE user_id = #{userId}")
    int deleteByUser(@Param("userId") long userId);

    /** 序列化用的 mapper。自建一份：与项目其它几处一致，容器并未暴露该 Bean。 */
    ObjectMapper JSON = new ObjectMapper();

    /**
     * 落库一份评估（序列化 + 插入），返回新行 id。
     *
     * <p>序列化失败**不吞**：说明 {@link InterviewEvaluation} 里有 Jackson 处理不了的东西，
     * 那是代码错误，应该在测试里就炸出来，而不是写一行空 JSON 进库。
     */
    default long insert(long userId, long conversationId, InterviewEvaluation evaluation) {
        EvaluationRow row = new EvaluationRow();
        row.setUserId(userId);
        row.setConversationId(conversationId);
        try {
            row.setScoreJson(JSON.writeValueAsString(evaluation));
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("评估结果无法序列化", failure);
        }
        row.setCreatedAt(Instant.now());
        insertRow(row);
        return row.getId() == null ? 0 : row.getId();
    }

    /** 读回一行并把 JSON 还原成对象。脏数据（手改过库）按"读不出来"处理，由调用方决定。 */
    default InterviewEvaluation toEvaluation(EvaluationRow row) {
        try {
            return JSON.readValue(row.getScoreJson(), InterviewEvaluation.class);
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("库里的评估 JSON 无法解析，id=" + row.getId(), failure);
        }
    }

    /** 一行评估（可变类 + `useGeneratedKeys`，与项目其它实体一致）。 */
    class EvaluationRow {
        private Long id;
        private Long userId;
        private Long conversationId;
        private String scoreJson;
        private Instant createdAt;

        Long getId() {
            return id;
        }

        void setId(Long id) {
            this.id = id;
        }

        Long getUserId() {
            return userId;
        }

        void setUserId(Long userId) {
            this.userId = userId;
        }

        Long getConversationId() {
            return conversationId;
        }

        void setConversationId(Long conversationId) {
            this.conversationId = conversationId;
        }

        String getScoreJson() {
            return scoreJson;
        }

        void setScoreJson(String scoreJson) {
            this.scoreJson = scoreJson;
        }

        Instant getCreatedAt() {
            return createdAt;
        }

        void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }
}
