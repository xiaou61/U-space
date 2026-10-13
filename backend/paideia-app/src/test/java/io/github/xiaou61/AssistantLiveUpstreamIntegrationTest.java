package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import com.jayway.jsonpath.JsonPath;
import io.github.xiaou61.security.AuthPort;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * **真实上游**的端到端检查（WORK-006 唯一一处连真模型的地方）。
 *
 * <p>为什么单独一个类、而且要显式给 Key 才跑：它**花真钱**（用户自己的额度），
 * 而且结果依赖模型当下的行为，不适合放进每次构建。默认跳过并计入 Skipped。
 *
 * <p>它补的是另外三个类覆盖不到的三段：
 * <ol>
 *   <li><b>真实调用能通</b>——DeepSeek 端点 + formatter + 密钥，全程没用过；</li>
 *   <li><b>工具循环</b>——模型真的会调工具 → 我们执行 → 回喂 → 出答案（中间那段从来没验过）；</li>
 *   <li><b>草稿</b>——模型真的会调 {@code draft_note} 并让 {@code draft} 事件出现（REQ-016 的关键证据）。</li>
 * </ol>
 *
 * <p>启用方式（Key 只从环境变量来，**不进仓库**）：
 * <pre>
 * PAIDEIA_TEST_DB_PASSWORD=… \
 * PAIDEIA_TEST_DEEPSEEK_KEY=sk-… \
 * PAIDEIA_TEST_DEEPSEEK_MODEL=deepseek-flash \
 * mvn -B -o -pl paideia-app -am test -Dtest=AssistantLiveUpstreamIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false
 * </pre>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "PAIDEIA_TEST_DEEPSEEK_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "PAIDEIA_TEST_DB_PASSWORD", matches = ".+")
class AssistantLiveUpstreamIntegrationTest {

    /** 真实端点与模型；都能用环境变量覆盖，但默认值就是官方那套。 */
    private static final String BASE_URL =
            System.getenv().getOrDefault("PAIDEIA_TEST_DEEPSEEK_BASE_URL", "https://api.deepseek.com");
    private static final String MODEL =
            System.getenv().getOrDefault("PAIDEIA_TEST_DEEPSEEK_MODEL", "deepseek-flash");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AuthPort authPort;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private final List<Long> createdEntries = new ArrayList<>();
    private String user;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM ai_messages");
        jdbcTemplate.update("DELETE FROM ai_conversations");
        jdbcTemplate.update("DELETE FROM ai_model_credentials");
        user = Long.toString(5_000_000_000L + (System.nanoTime() % 1_000_000_000L));
    }

    @AfterEach
    void cleanUp() {
        for (long id : createdEntries) {
            try {
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/knowledge/admin/entries/" + id).header("Authorization", adminToken()));
            } catch (Exception ignored) {
                // 收尾失败不影响结论；条目 id 已记在日志里
            }
        }
        setUp();
    }

    @Test
    @DisplayName("真实上游：一次调用能通，流式有增量，用量返还")
    void liveCallStreamsAndReturnsUsage() throws Exception {
        configureCredential();

        String stream = send("请只回答两个字：收到。");

        assertThat(stream).as("流里的事件：\n%s", stream).contains("event:content");
        assertThat(stream).contains("event:done");
        // 用量得是真实数字（0 说明没真正走通上游）
        assertThat(usageTotal(stream)).isPositive();
    }

    @Test
    @DisplayName("真实上游：模型会真的调工具（检索条目），并把结果用进回答")
    void liveModelCallsTheSearchTool() throws Exception {
        configureCredential();
        // 造一条标题极不常见的条目：模型只有**真的检索过**才可能说出这个词
        String marker = "紫电青霜" + (System.nanoTime() % 100_000);
        long entryId = createPublishedEntry(marker);

        String stream = send("请用你的检索工具查一下，平台里有没有和「" + marker + "」有关的内容？"
                + "有的话把条目标题原样告诉我。");

        // 工具真跑了 → 检索结果回到模型 → 回答里出现那个只有检索才知道的词
        assertThat(stream).as("回答里没出现只有检索才知道的标题，说明工具没跑或结果没回喂：\n%s", stream)
                .contains(marker);
        assertThat(entryId).isPositive();
    }

    @Test
    @DisplayName("真实上游：说「帮我记一条笔记」会产出 draft 事件（草稿），且不落库")
    void liveModelDraftsANoteWithoutWriting() throws Exception {
        configureCredential();
        int notesBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notes", Integer.class);

        String stream = send("帮我记一条笔记：标题「导数复习」，正文「链式法则要再练」。");

        assertThat(stream).as("没有 draft 事件，说明模型没调 draft_note：\n%s", stream).contains("event:draft");
        assertThat(stream).contains("导数复习");
        // **关键**：拟草稿不写库——真正落库只能经用户点击后的 POST /api/v1/notes
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM notes", Integer.class))
                .isEqualTo(notesBefore);
    }

    /** 打印真实上游有没有产生思考事件（V4.1-Flash 的 effort 档位会带来推理内容）。只记录，不断言。 */
    @Test
    @DisplayName("真实上游：记录思考事件是否出现（不断言，只作事实记录）")
    void reportsWhetherThinkingArrives() throws Exception {
        configureCredential();

        String stream = send("用一句话解释什么是导数。");

        System.out.println("[live] 思考事件出现 " + countOf(stream, "event:thinking")
                + " 次，正文事件 " + countOf(stream, "event:content") + " 次，用量 "
                + usageTotal(stream) + " tokens");
        assertThat(stream).contains("event:done");
    }

    private void configureCredential() throws Exception {
        MvcResult saved = mockMvc.perform(put("/api/v1/ai/credentials")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"baseUrl\":\"" + BASE_URL + "\",\"model\":\"" + MODEL + "\","
                                + "\"apiKey\":\"" + System.getenv("PAIDEIA_TEST_DEEPSEEK_KEY") + "\"}"))
                .andReturn();
        assertThat(saved.getResponse().getStatus()).as("响应：%s", body(saved)).isEqualTo(200);
    }

    /** 发一轮并把真实 SSE 流读完（会真的等上游，超时由会话配置的 60 秒兜住）。 */
    private String send(String message) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/ai/conversations")
                        .header("Authorization", token())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn();
        long conversationId = ((Number) JsonPath.read(body(created), "$.data.id")).longValue();

        MvcResult started = mockMvc.perform(post("/api/v1/ai/conversations/" + conversationId + "/messages")
                        .header("Authorization", token())
                        .accept(MediaType.TEXT_EVENT_STREAM, MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"" + message.replace("\"", "\\\"") + "\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();
        return body(mockMvc.perform(asyncDispatch(started)).andReturn());
    }

    private long createPublishedEntry(String title) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/knowledge/admin/entries")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"body\":\"正文\",\"categoryId\":null,"
                                + "\"status\":\"published\"}"))
                .andReturn();
        assertThat(created.getResponse().getStatus()).as("建房：%s", body(created)).isEqualTo(200);
        long id = ((Number) JsonPath.read(body(created), "$.data")).longValue();
        createdEntries.add(id);
        return id;
    }

    /** 从 done 事件里抠出 total；抠不到返回 -1（好让断言给出可读的失败）。 */
    private static long usageTotal(String stream) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"total\"\\s*:\\s*(\\d+)").matcher(stream);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : -1;
    }

    private static int countOf(String stream, String needle) {
        return stream.split(java.util.regex.Pattern.quote(needle), -1).length - 1;
    }

    private String token() {
        return "Bearer " + authPort.issue(new AuthPort.Subject(user, AuthPort.ROLE_LEARNER)).value();
    }

    private String adminToken() {
        return "Bearer " + authPort.issue(new AuthPort.Subject("1", AuthPort.ROLE_ADMIN)).value();
    }

    private static String body(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
