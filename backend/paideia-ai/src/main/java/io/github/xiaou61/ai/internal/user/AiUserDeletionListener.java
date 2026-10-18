package io.github.xiaou61.ai.internal.user;

import io.github.xiaou61.platform.UserDeletedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 注销时删掉本模块的用户数据（用户裁决 10：级联硬删）。
 *
 * <p><b>这里是一张手写的表清单</b>：项目**不建跨模块外键**（见 V4 的约定），数据库不会替我们
 * 级联。清单的正确性由 {@code AccountDeletionCascadeIntegrationTest} 盯着——它在注销后逐表断言
 * 行数为 0，**新增用户私有表却忘了登记，那条用例就会红**。
 *
 * <p>清单里包含 WORK-005 就有的两张表（凭据、精简结果）：注销后留下加密的 API Key 尤其不对，
 * 那是这个人留在这里的一把钥匙。
 *
 * <p><b>表名与列名要成对写</b>：本模块里 `ai_messages` **没有** `user_id`（它随会话走），
 * 按 `user_id` 一刀切会直接 SQL 报错。
 *
 * <p>用 {@code BEFORE_COMMIT}：与账号模块的删除在**同一个事务**里，要么全删干净、要么什么都没发生。
 * 分成两个事务会出现"用户没了但记忆还在"的中间状态，而那种残留没有任何人会去清理。
 */
@Component
class AiUserDeletionListener {

    /** 表 + 用于定位这个人的列。子表在前。 */
    private static final String[][] USER_TABLES = {
            {"ai_memories_structured", "user_id"},
            {"ai_memories_text", "user_id"},
            // 三个开关（WORK-013）：注销就该一并消失，留着一行等于"这个人还在这里有个偏好"
            {"ai_user_settings", "user_id"},
            // 面试评估（WORK-010）：这个人留下的评分与弱项，注销就该一起消失
            {"ai_interview_evaluations", "user_id"},
            {"ai_model_credentials", "user_id"},
            {"ai_entry_summaries", "owner_user_id"},
            // 积分流水与账户（WORK-009）。**注销一并删**（用户决定 8）：守住"注销即级联硬删"，
            // 代价是注销用户的账不再完整——这是明确选过的方向，别改成"保留审计"。
            {"ai_credit_ledger", "user_id"},
            {"ai_credit_accounts", "user_id"},
            // 会话最后删：消息跟着它（下面单独删）
            {"ai_conversations", "user_id"},
    };

    private final JdbcTemplate jdbcTemplate;

    AiUserDeletionListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void onUserDeleted(UserDeletedEvent event) {
        // 消息没有 user_id：先按"属于这个人的会话"删，再删会话本身
        jdbcTemplate.update("""
                DELETE FROM ai_messages
                WHERE conversation_id IN (SELECT id FROM ai_conversations WHERE user_id = ?)
                """, event.userId());
        for (String[] table : USER_TABLES) {
            jdbcTemplate.update("DELETE FROM " + table[0] + " WHERE " + table[1] + " = ?", event.userId());
        }
    }
}
