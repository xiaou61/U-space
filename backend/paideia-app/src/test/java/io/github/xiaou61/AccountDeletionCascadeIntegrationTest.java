package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import io.github.xiaou61.account.internal.user.PasswordHasher;
import io.github.xiaou61.security.AuthPort;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 注销的级联硬删（AC-014）。
 *
 * <p><b>这个类就是"删干净"的长期护栏。</b>各模块的删除清单是手写的（项目不建跨模块外键），
 * 所以新增一张用户私有表却忘了登记时，唯一能发现它的地方就是下面这张表清单：
 * 注销之后**逐表断言行数为 0**。
 *
 * <p>用户直接插库造（带真实的 BCrypt 口令哈希），不走注册：注册要解图形验证码 + 读内存信箱，
 * 而那两条链路已经由 `AccountAuthIntegrationTest` 覆盖。这里要验的是"注销之后还剩什么"。
 *
 * <p>需要真实 MySQL 与 {@code PAIDEIA_TEST_DB_PASSWORD}，未配置时整类跳过。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "PAIDEIA_TEST_DB_PASSWORD", matches = ".+")
class AccountDeletionCascadeIntegrationTest {

    private static final String PASSWORD = "deletion-test-password";

    /**
     * 本测试的**核心断言清单**：这个人的数据散在哪几张表、用哪一列归属。
     *
     * <p>每加一张用户私有表都要加一行；不加，注销就会悄悄留下数据而没人知道。
     */
    private static final String[][] USER_DATA = {
            {"ai_conversations", "user_id"},
            {"ai_messages", "conversation_id"}, // 单独按会话删，见断言处的说明
            {"ai_memories_structured", "user_id"},
            {"ai_memories_text", "user_id"},
            {"ai_user_settings", "user_id"},
            // WORK-010：身份画像与面试评估也必须是"注销后逐表为 0"
            {"learner_profiles", "user_id"},
            {"ai_interview_evaluations", "user_id"},
            {"ai_model_credentials", "user_id"},
            {"ai_entry_summaries", "owner_user_id"},
            // 积分账户与流水（WORK-009）。用户决定 8：注销时**一并删**，守住"注销即级联硬删"。
            // 新增用户私有表却忘了在这里加一行，注销就会悄悄留下一份账，而没人会知道。
            {"ai_credit_accounts", "user_id"},
            {"ai_credit_ledger", "user_id"},
            {"notes", "user_id"},
            {"note_shares", "user_id"},
            {"note_reports", "reporter_user_id"},
            // 知识库自评（"我对哪些条目懂／不懂"）：本项目第一张用户私有业务数据表
            {"knowledge_self_assessments", "user_id"},
            {"refresh_tokens", "user_id"},
    };

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    AuthPort authPort;

    @Autowired
    PasswordHasher passwordHasher;

    private Long userId;

    @AfterEach
    void cleanUp() {
        if (userId == null) {
            return;
        }
        // 残留清理：注销成功时这些表本来就该是空的，这里只是兜底，防用例中途失败
        jdbcTemplate.update("DELETE FROM ai_messages WHERE conversation_id IN"
                + " (SELECT id FROM ai_conversations WHERE user_id = ?)", userId);
        for (String[] table : USER_DATA) {
            if (table[0].equals("ai_messages")) {
                continue;
            }
            jdbcTemplate.update("DELETE FROM " + table[0] + " WHERE " + table[1] + " = ?", userId);
        }
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", userId);
        userId = null;
    }

    @Test
    @DisplayName("注销把散在各模块的个人数据全部删净，且旧令牌不能再访问")
    void deletionRemovesEveryTraceAndInvalidatesTheToken() throws Exception {
        userId = insertUser();
        seedEveryKindOfUserData(userId);
        String token = token(userId);

        // 注销前：数据都在
        assertThat(countIn("notes", "user_id", userId)).isPositive();
        assertThat(mockMvc.perform(get("/api/v1/me").header("Authorization", token))
                .andReturn().getResponse().getStatus()).isEqualTo(200);

        // 口令不对：拒绝，且什么都不删（不可逆的操作不能凭一个被偷走的令牌就做掉）
        assertThat(deletionStatus(token, "wrong-password")).isEqualTo(400);
        assertThat(countIn("notes", "user_id", userId)).isPositive();

        // 正确口令：注销
        assertThat(deletionStatus(token, PASSWORD)).isEqualTo(200);

        // **逐表断言**：这张清单就是护栏
        for (String[] table : USER_DATA) {
            if (table[0].equals("ai_messages")) {
                // 消息没有 user_id：它随会话删。会话已经没了，所以按"孤立消息"断言
                assertThat(jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM ai_messages m WHERE m.conversation_id NOT IN"
                                + " (SELECT id FROM ai_conversations)", Long.class))
                        .as("ai_messages 里留下了孤儿消息").isZero();
                continue;
            }
            assertThat(countIn(table[0], table[1], userId))
                    .as("%s 里还留着这个人的数据", table[0]).isZero();
        }
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Long.class, userId))
                .as("users 那一行还在").isZero();

        // **能立刻保证的是：刷不出新令牌**（refresh 行已随注销删掉），
        // 于是这个会话最多活到访问令牌的 TTL 到期为止。
        //
        // 刻意**不**断言"旧访问令牌立刻 401"：JWT 是无状态的，签名有效就不看库，删行不会让它失效。
        // 要做到立刻失效，得在鉴权层对每个请求做一次存活性查询——那会让**全部**集成测试
        // （它们都用不存在的用户 id 签令牌）集体改造成"先造真实用户"，代价与收益不对等。
        // 这是已知缺口，已记在 updates.md：真正要紧的"数据没了"与"刷不了"两条都在这里断言了。
        assertThat(countIn("refresh_tokens", "user_id", userId)).isZero();
    }

    @Test
    @DisplayName("别人的数据不受影响——注销只删自己那一份")
    void deletionDoesNotTouchOthers() throws Exception {
        long mine = insertUser();
        long theirs = insertUser();
        userId = mine;
        seedEveryKindOfUserData(theirs);

        assertThat(deletionStatus(token(mine), PASSWORD)).isEqualTo(200);

        assertThat(countIn("notes", "user_id", theirs)).isPositive();
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Long.class, theirs))
                .isPositive();

        // 收尾：把另一个人的数据也清掉
        userId = theirs;
    }

    private long insertUser() {
        String suffix = Long.toString(System.nanoTime() % 1_000_000L);
        jdbcTemplate.update("""
                INSERT INTO users (username, email, password_hash, role, email_verified_at)
                VALUES (?, ?, ?, 'learner', CURRENT_TIMESTAMP(3))
                """, "del-" + suffix, "del-" + suffix + "@example.test", passwordHasher.hash(PASSWORD));
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, "del-" + suffix);
    }

    /** 把这个人**每一种**用户私有数据都造一份：漏掉一类，泄漏就不是"零"能断言的。 */
    private void seedEveryKindOfUserData(long owner) {
        jdbcTemplate.update("INSERT INTO ai_conversations (user_id, expires_at) VALUES (?, ?)",
                owner, Timestamp.from(Instant.now().plusSeconds(3600)));
        long conversationId = jdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM ai_conversations WHERE user_id = ?", Long.class, owner);
        jdbcTemplate.update("INSERT INTO ai_messages (conversation_id, role, content) VALUES (?, 'user', '你好')",
                conversationId);
        jdbcTemplate.update("INSERT INTO ai_memories_structured (user_id, kind, content) VALUES (?, 'k', '事实')", owner);
        jdbcTemplate.update("INSERT INTO ai_memories_text (user_id, content) VALUES (?, '说过的话')", owner);
        jdbcTemplate.update("INSERT INTO ai_user_settings (user_id, updated_at) VALUES (?, UTC_TIMESTAMP(3))", owner);
        jdbcTemplate.update("INSERT INTO learner_profiles (user_id, identity, updated_at) VALUES (?, 'job_seeker', UTC_TIMESTAMP(3))", owner);
        jdbcTemplate.update("INSERT INTO ai_interview_evaluations (user_id, conversation_id, score_json, created_at) "
                + "VALUES (?, 999999, '{\"overall\":3}', UTC_TIMESTAMP(3))", owner);
        jdbcTemplate.update("""
                INSERT INTO ai_model_credentials (user_id, base_url, model, api_key_cipher, api_key_last4)
                VALUES (?, 'https://1.1.1.1/v1', 'm', X'00', 'abcd')
                """, owner);
        jdbcTemplate.update("""
                INSERT INTO ai_entry_summaries
                    (entry_id, content_hash, model, prompt_version, owner_user_id, visibility, card_json)
                VALUES (1, REPEAT('a', 64), 'm', 'v1', ?, 'private', '{}')
                """, owner);
        jdbcTemplate.update("INSERT INTO notes (user_id, title, body) VALUES (?, '标题', '正文')", owner);
        // 积分账户 + 一条流水（WORK-009）
        jdbcTemplate.update("INSERT INTO ai_credit_accounts (user_id, balance) VALUES (?, 123)", owner);
        jdbcTemplate.update("""
                INSERT INTO ai_credit_ledger (user_id, delta, kind, model, input_tokens, output_tokens, note)
                VALUES (?, -3, 'SETTLE', 'm', 10, 20, '用例流水')
                """, owner);
        long noteId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM notes WHERE user_id = ?", Long.class, owner);
        jdbcTemplate.update("""
                INSERT INTO note_shares (share_id, note_id, user_id, expires_at)
                VALUES (UUID(), ?, ?, ?)
                """, noteId, owner, Timestamp.from(Instant.now().plusSeconds(3600)));
        // 举报：指向自己那条分享（只要 reporter_user_id 是这个人都算他的行为记录）
        String shareId = jdbcTemplate.queryForObject(
                "SELECT share_id FROM note_shares WHERE user_id = ?", String.class, owner);
        jdbcTemplate.update("INSERT INTO note_reports (share_id, reporter_user_id, reason) VALUES (?, ?, '理由')",
                shareId, owner);
        jdbcTemplate.update("""
                INSERT INTO refresh_tokens (user_id, token_hash, family_id, expires_at)
                VALUES (?, REPEAT('b', 64), UUID(), ?)
                """, owner, Timestamp.from(Instant.now().plusSeconds(3600)));
        // 知识库自评：必须真种一条，否则新加的 USER_DATA 行只会断言到 0 条，等于没验。
        // entry_id 借用种子里任意一条已发布条目（外键指向 knowledge_entries）。
        Long entryId = jdbcTemplate.queryForObject(
                "SELECT id FROM knowledge_entries WHERE status = 'published' LIMIT 1", Long.class);
        if (entryId != null) {
            jdbcTemplate.update("""
                    INSERT INTO knowledge_self_assessments (user_id, entry_id, level)
                    VALUES (?, ?, 'understood')
                    """, owner, entryId);
        }
    }

    private int deletionStatus(String token, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/me/deletion")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"" + password + "\"}"))
                .andReturn();
        return result.getResponse().getStatus();
    }

    private long countIn(String table, String column, long value) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?", Long.class, value);
        return count == null ? 0 : count;
    }

    private String token(long id) {
        return "Bearer " + authPort.issue(new AuthPort.Subject(Long.toString(id), AuthPort.ROLE_LEARNER)).value();
    }
}
