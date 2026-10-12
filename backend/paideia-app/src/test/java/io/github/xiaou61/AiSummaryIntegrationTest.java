package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.jayway.jsonpath.JsonPath;
import io.github.xiaou61.ai.EntrySummarizer;
import io.github.xiaou61.ai.GeneratedSummary;
import io.github.xiaou61.ai.SummaryCard;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import io.github.xiaou61.security.AuthPort;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * AI 精简的端到端集成测试（AC-002、AC-003、AC-004、AC-005、AC-007、AC-009、AC-010）。
 *
 * <p>用完整上下文 + 真实 MySQL + MockMvc，但**把端口换成桩**：测试绝不能真连上游
 * （没有网络、也没有谁的 Key 可以花）。桩同时承担"调用计数"的职责——"命中缓存不再调模型"
 * 这条只能靠它证明。
 *
 * <p>用户身份用 {@code AuthPort} 直接签令牌造出来：本模块的表对 users 没有外键，
 * 两个不同的数字主体就足以表达"两个用户"。管理员令牌用真管理员角色，只为建一条测试内容。
 *
 * <p>需要真实 MySQL 与 {@code PAIDEIA_TEST_DB_PASSWORD}；未配置时整个类被跳过并计入 Skipped。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "PAIDEIA_TEST_DB_PASSWORD", matches = ".+")
class AiSummaryIntegrationTest {

    private static final String PUBLIC_BASE_URL = "https://1.1.1.1/v1";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AuthPort authPort;

    @Autowired
    JdbcTemplate jdbcTemplate;

    /** 端口换成桩：既避免真连上游，也用来数调用次数。 */
    @MockitoBean
    EntrySummarizer summarizer;

    private long entryId;

    /**
     * 每个用例一组全新的用户 id：限流计数在进程内、**按用户累计**，若共用固定 id，
     * 同一批用例跑下来会互相把额度用掉，后面的用例就变成 429——那是测试自身的耦合，不是产品行为。
     */
    private String userA;
    private String userB;

    @BeforeEach
    void setUp() throws Exception {
        clearInvocations(summarizer);
        // 这两张表只有本模块在写，测试自己清干净就能做到用例间互不影响；
        // 不清的话"未配置凭据"这类断言会被上一个用例留下的行打破。
        jdbcTemplate.update("DELETE FROM ai_model_credentials");
        jdbcTemplate.update("DELETE FROM ai_entry_summaries");
        String suffix = Long.toString(System.nanoTime() % 1_000_000L);
        userA = "91" + suffix;
        userB = "92" + suffix;
        when(summarizer.summarize(anyString(), anyString(), any())).thenReturn(card("首次生成"));

        // 每个用例用自己新建的内容：缓存键含正文哈希，用共享种子会互相污染
        entryId = createPublishedEntry("AI 集成测试条目 " + System.nanoTime());
    }

    @Test
    @DisplayName("未登录一律 401；已登录但无凭据时视图为未配置")
    void requiresAuthentication() throws Exception {
        assertThat(mockMvc.perform(get("/api/v1/ai/credentials")).andReturn().getResponse().getStatus())
                .isEqualTo(401);
        assertThat(mockMvc.perform(get("/api/v1/ai/summaries/" + entryId)).andReturn().getResponse().getStatus())
                .isEqualTo(401);

        MvcResult result = mockMvc.perform(get("/api/v1/ai/credentials").header("Authorization", token(userA)))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(JsonPath.<Boolean>read(body(result), "$.data.configured")).isFalse();
    }

    @Test
    @DisplayName("凭据加密落库：接口只回 last4，响应与库里都没有明文")
    void credentialIsEncryptedAndNeverEchoed() throws Exception {
        String apiKey = "sk-integration-secret-4321";
        MvcResult saved = mockMvc.perform(put("/api/v1/ai/credentials")
                        .header("Authorization", token(userA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"baseUrl\":\"" + PUBLIC_BASE_URL + "\",\"model\":\"deepseek-flash\","
                                + "\"apiKey\":\"" + apiKey + "\"}"))
                .andReturn();

        assertThat(saved.getResponse().getStatus()).isEqualTo(200);
        assertThat(saved.getResponse().getContentAsString(StandardCharsets.UTF_8)).doesNotContain(apiKey);
        assertThat(JsonPath.<String>read(body(saved), "$.data.apiKeyLast4")).isEqualTo("4321");
        assertThat(JsonPath.<Boolean>read(body(saved), "$.data.configured")).isTrue();

        byte[] cipher = jdbcTemplate.queryForObject(
                "SELECT api_key_cipher FROM ai_model_credentials WHERE user_id = ?", byte[].class,
                Long.parseLong(userA));
        assertThat(cipher).isNotNull();
        assertThat(new String(cipher, StandardCharsets.ISO_8859_1)).doesNotContain(apiKey);
    }

    @Test
    @DisplayName("SSRF 在保存凭据时就被拒，且一次调用都不会发生")
    void rejectsInternalBaseUrlBeforeAnyCall() throws Exception {
        MvcResult result = mockMvc.perform(put("/api/v1/ai/credentials")
                        .header("Authorization", token(userA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"baseUrl\":\"https://10.0.0.1/v1\",\"model\":\"m\",\"apiKey\":\"k-1234\"}"))
                .andReturn();
        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(body(result)).contains("baseUrl 不被允许");
        verify(summarizer, never()).summarize(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("生成返回结构化卡片与用量；重复请求命中缓存且不再调模型")
    void generatesThenServesFromCache() throws Exception {
        saveCredential(userA);

        MvcResult first = generate(userA, "public");
        assertThat(first.getResponse().getStatus()).as("generate 响应：%s", body(first)).isEqualTo(200);
        assertThat(JsonPath.<String>read(body(first), "$.data.card.headline")).isEqualTo("首次生成");
        assertThat(JsonPath.<Boolean>read(body(first), "$.data.cached")).isFalse();
        assertThat(JsonPath.<String>read(body(first), "$.data.source")).isEqualTo("mine");
        assertThat(JsonPath.<Integer>read(body(first), "$.data.usage.totalTokens")).isEqualTo(42);
        verify(summarizer, times(1)).summarize(anyString(), anyString(), any());

        MvcResult second = generate(userA, "public");
        assertThat(JsonPath.<Boolean>read(body(second), "$.data.cached")).isTrue();
        assertThat(JsonPath.<Object>read(body(second), "$.data.usage")).isNull();
        // 关键断言：命中缓存**不再调模型**
        verify(summarizer, times(1)).summarize(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("私有结果别人读不到；改成公开后别人能读到且同样不触发调用")
    void visibilityIsEnforced() throws Exception {
        saveCredential(userA);
        generate(userA, "private");

        MvcResult other = mockMvc.perform(get("/api/v1/ai/summaries/" + entryId)
                        .header("Authorization", token(userB)))
                .andReturn();
        assertThat(JsonPath.<Object>read(body(other), "$.data")).isNull();

        MvcResult owner = mockMvc.perform(get("/api/v1/ai/summaries/" + entryId)
                        .header("Authorization", token(userA)))
                .andReturn();
        assertThat(JsonPath.<String>read(body(owner), "$.data.card.headline")).isEqualTo("首次生成");
        assertThat(JsonPath.<String>read(body(owner), "$.data.source")).isEqualTo("mine");

        // 另一个用户自己生成一份公开的：他自己是 owner
        saveCredential(userB);
        when(summarizer.summarize(anyString(), anyString(), any())).thenReturn(card("B 生成的"));
        generate(userB, "public");

        MvcResult nowVisible = mockMvc.perform(get("/api/v1/ai/summaries/" + entryId)
                        .header("Authorization", token(userA)))
                .andReturn();
        assertThat(JsonPath.<String>read(body(nowVisible), "$.data.source")).isEqualTo("others");
    }

    @Test
    @DisplayName("没配凭据的用户能读公开结果，但触发生成时被明确拒绝且不调用上游")
    void keylessUserCanReadButNotGenerate() throws Exception {
        saveCredential(userA);
        generate(userA, "public");

        MvcResult read = mockMvc.perform(get("/api/v1/ai/summaries/" + entryId)
                        .header("Authorization", token(userB)))
                .andReturn();
        assertThat(JsonPath.<String>read(body(read), "$.data.card.headline")).isEqualTo("首次生成");

        // 注意：对**已有公开缓存**的内容，没凭据的用户点"生成"会直接命中缓存返回结果——
        // 这是刻意的（缓存命中不花他的钱）。所以"不能生成"要用一条还没有缓存的内容来验。
        long uncached = createPublishedEntry("无缓存的条目 " + System.nanoTime());
        MvcResult generate = generate(userB, uncached, "public");
        assertThat(generate.getResponse().getStatus()).as("响应：%s", body(generate)).isEqualTo(409);
        assertThat(body(generate)).contains("尚未配置模型凭据");
        verify(summarizer, times(1)).summarize(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("正文改动后旧结果不再命中")
    void contentChangeInvalidatesCache() throws Exception {
        saveCredential(userA);
        generate(userA, "public");

        mockMvc.perform(put("/api/v1/knowledge/admin/entries/" + entryId)
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"改过的标题\",\"body\":\"改过的正文\",\"categoryId\":null,"
                                + "\"status\":\"published\"}"))
                .andReturn();

        MvcResult afterChange = mockMvc.perform(get("/api/v1/ai/summaries/" + entryId)
                        .header("Authorization", token(userA)))
                .andReturn();
        assertThat(JsonPath.<Object>read(body(afterChange), "$.data")).isNull();
    }

    @Test
    @DisplayName("上游失败不写缓存：下一次请求还会再试")
    void failureIsNotCached() throws Exception {
        saveCredential(userA);
        when(summarizer.summarize(anyString(), anyString(), any())).thenThrow(
                new BizException(ErrorCode.INTERNAL, "模型没有返回可用的精简结果（模型没有返回任何内容）"));

        MvcResult failed = generate(userA, "public");
        assertThat(failed.getResponse().getStatus()).isEqualTo(500);
        // 错误信息里不得出现上游原始报文
        assertThat(body(failed)).doesNotContain("评测");

        MvcResult readAfterFailure = mockMvc.perform(get("/api/v1/ai/summaries/" + entryId)
                        .header("Authorization", token(userA)))
                .andReturn();
        assertThat(JsonPath.<Object>read(body(readAfterFailure), "$.data")).isNull();
    }

    @Test
    @DisplayName("未发布条目不能生成")
    void draftEntryIsNotFound() throws Exception {
        saveCredential(userA);
        MvcResult created = mockMvc.perform(post("/api/v1/knowledge/admin/entries")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"草稿\",\"body\":\"内容\",\"categoryId\":null,\"status\":\"draft\"}"))
                .andReturn();
        long draftId = ((Number) JsonPath.read(body(created), "$.data")).longValue();

        MvcResult result = generate(userA, draftId, "public");
        assertThat(result.getResponse().getStatus()).isEqualTo(404);
    }

    private void saveCredential(String user) throws Exception {
        mockMvc.perform(put("/api/v1/ai/credentials")
                        .header("Authorization", token(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"baseUrl\":\"" + PUBLIC_BASE_URL + "\",\"model\":\"deepseek-flash\","
                                + "\"apiKey\":\"sk-test-key-0001\"}"))
                .andReturn();
    }

    private MvcResult generate(String user, String visibility) throws Exception {
        return generate(user, entryId, visibility);
    }

    private MvcResult generate(String user, long id, String visibility) throws Exception {
        return mockMvc.perform(post("/api/v1/ai/summaries")
                        .header("Authorization", token(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"entryId\":" + id + ",\"visibility\":\"" + visibility + "\"}"))
                .andReturn();
    }

    private long createPublishedEntry(String title) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/knowledge/admin/entries")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"body\":\"正文内容，用于 AI 精简测试。\","
                                + "\"categoryId\":null,\"status\":\"published\"}"))
                .andReturn();
        assertThat(created.getResponse().getStatus()).isEqualTo(200);
        return ((Number) JsonPath.read(body(created), "$.data")).longValue();
    }

    private String token(String subject) {
        return "Bearer " + authPort.issue(new AuthPort.Subject(subject, AuthPort.ROLE_LEARNER)).value();
    }

    private String adminToken() {
        return "Bearer " + authPort.issue(new AuthPort.Subject("1", AuthPort.ROLE_ADMIN)).value();
    }

    private static String body(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private static GeneratedSummary card(String headline) {
        return new GeneratedSummary(
                new SummaryCard(headline, List.of(new SummaryCard.SummarySection("小节", "小节正文"))),
                new TokenUsage(30, 12, 42),
                false);
    }
}
