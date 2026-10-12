package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.jayway.jsonpath.JsonPath;
import io.github.xiaou61.ai.internal.tools.AssistantTools;
import io.github.xiaou61.security.AuthPort;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 助手只读工具（AC-007）。
 *
 * <p>测的是**工具本身**：检索只给已发布、自评标记只给本人、参数越界被夹住、未知工具名不炸。
 * **工具循环本身（模型要工具→执行→回喂）没有自动化覆盖**：那需要一个真实上游，本环境没有，
 * 也不该花谁的 Key。这与 WORK-005 留下的缺口同性质，已记在验证报告里。
 *
 * <p>需要真实 MySQL 与 {@code PAIDEIA_TEST_DB_PASSWORD}，未配置时整类跳过。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "PAIDEIA_TEST_DB_PASSWORD", matches = ".+")
class AssistantToolsIntegrationTest {

    @Autowired
    AssistantTools tools;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AuthPort authPort;

    private final List<Long> createdEntries = new ArrayList<>();
    private String userA;
    private String userB;

    @BeforeEach
    void setUp() {
        createdEntries.clear();
        String suffix = Long.toString(System.nanoTime() % 1_000_000L);
        userA = "61" + suffix;
        userB = "62" + suffix;
    }

    @AfterEach
    void cleanUp() {
        // 自己建的条目自己删：测试库是共享的，留下已发布条目会把知识库那边的列表断言弄红
        for (long id : createdEntries) {
            try {
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/v1/knowledge/admin/entries/" + id).header("Authorization", adminToken()));
            } catch (Exception ignored) {
                // 收尾失败不该让用例结论变得含糊；id 已记在日志里
            }
        }
    }

    @Test
    @DisplayName("search_entries 只返回已发布条目——草稿不能经助手漏出去")
    void searchOnlyReturnsPublished() throws Exception {
        String marker = "工具检索" + System.nanoTime();
        long published = createEntry(marker + "甲", "published");
        createEntry(marker + "乙", "draft");

        AssistantTools.Outcome outcome = tools.run(Long.parseLong(userA), "search_entries", Map.of("keyword", marker));

        assertThat(outcome.error()).isFalse();
        assertThat(outcome.text()).contains("id=" + published + " 标题=" + marker + "甲");
        assertThat(outcome.text()).doesNotContain(marker + "乙");
    }

    @Test
    @DisplayName("检索不到时给明确说法，而不是空串")
    void searchSaysSoWhenNothingFound() {
        AssistantTools.Outcome outcome = tools.run(Long.parseLong(userA), "search_entries",
                Map.of("keyword", "绝不可能存在的词" + System.nanoTime()));

        assertThat(outcome.error()).isFalse();
        assertThat(outcome.text()).contains("没有检索到");
    }

    @Test
    @DisplayName("list_categories 给出 id／名称／上级，层级可判")
    void listCategoriesReturnsShapedRows() {
        AssistantTools.Outcome outcome = tools.run(Long.parseLong(userA), "list_categories", Map.of());

        assertThat(outcome.error()).isFalse();
        assertThat(outcome.text()).isNotBlank();
        assertThat(outcome.text()).containsPattern("id=\\d+ 名称=.+ 上级=(无（顶级）|\\d+)");
    }

    @Test
    @DisplayName("my_marks 只给**调用者自己**的标记，别人的拿不到")
    void marksBelongToTheCallerOnly() throws Exception {
        long mineEntry = createEntry("甲的标记条目" + System.nanoTime(), "published");
        long theirsEntry = createEntry("乙的标记条目" + System.nanoTime(), "published");
        assertThat(mockMvc.perform(put("/api/v1/knowledge/entries/" + mineEntry + "/self-assessment")
                        .header("Authorization", token(userA))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"level\":\"unsure\"}"))
                .andReturn().getResponse().getStatus()).isEqualTo(200);
        assertThat(mockMvc.perform(put("/api/v1/knowledge/entries/" + theirsEntry + "/self-assessment")
                        .header("Authorization", token(userB))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"level\":\"unsure\"}"))
                .andReturn().getResponse().getStatus()).isEqualTo(200);

        AssistantTools.Outcome outcome = tools.run(Long.parseLong(userA), "my_marks", Map.of());

        assertThat(outcome.error()).isFalse();
        assertThat(outcome.text()).contains("id=" + mineEntry);
        assertThat(outcome.text()).doesNotContain("id=" + theirsEntry);
    }

    @Test
    @DisplayName("未知工具名返回错误结果而不是抛异常——工具失败不该炸掉整轮对话")
    void unknownToolReturnsErrorOutcome() {
        AssistantTools.Outcome outcome = tools.run(Long.parseLong(userA), "delete_everything", Map.of());

        assertThat(outcome.error()).isTrue();
        assertThat(outcome.text()).contains("没有名为");
    }

    @Test
    @DisplayName("条数参数不可信：给 1000 夹到上限，给 0／负数／字符串都退回默认而不是崩")
    void limitIsClamped() throws Exception {
        String marker = "上限" + System.nanoTime();
        for (int index = 0; index < 3; index++) {
            createEntry(marker + index, "published");
        }

        // 三种坏输入都必须安全返回
        for (Object limit : List.of(1000, 0, -1, "不是数字")) {
            AssistantTools.Outcome outcome =
                    tools.run(Long.parseLong(userA), "search_entries", Map.of("keyword", marker, "limit", limit));
            assertThat(outcome.error()).as("limit=%s", limit).isFalse();
            assertThat(outcome.text()).contains(marker + "0");
        }

        // 上限 20：即便要 1000 条也不会把上下文塞爆（这里只有 3 条，验的是"不报错且不超上限"）
        AssistantTools.Outcome capped = tools.run(Long.parseLong(userA), "search_entries",
                Map.of("keyword", marker, "limit", 1000));
        assertThat(capped.text().lines().count()).isLessThanOrEqualTo(20);
    }

    private long createEntry(String title, String status) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/knowledge/admin/entries")
                        .header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"body\":\"正文\",\"categoryId\":null,"
                                + "\"status\":\"" + status + "\"}"))
                .andReturn();
        assertThat(created.getResponse().getStatus()).as("建房：%s", body(created)).isEqualTo(200);
        long id = ((Number) JsonPath.read(body(created), "$.data")).longValue();
        createdEntries.add(id);
        return id;
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
}
