package io.github.xiaou61;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.xiaou61.security.AuthPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 知识库只读端点的集成测试（真实 MySQL）。
 *
 * <p><b>数据来自 e2e 种子</b>：`db/devdata` 在 {@code application-test.yml} 的 Flyway locations 里，
 * 与端到端测试用的是同一份数据、同一种加载方式——种子本来就是为"可复现地覆盖每条查询路径"
 * 而写的（两棵子树、三条已发布、一条草稿、两条关系）。这样不必为了测试再造一份夹具，
 * 避免两处数据各说各话。
 *
 * <p><b>种子 id 现查，不写死</b>（2026-10-18 改）：写死 id 的断言会被"同一个库上的其他写入"连累——
 * 全量跑时被别的会话删掉种子行，本类就报"读不到 id=1"，而每类单跑都绿、看起来像产品坏了。
 * 现在按标题查（见 {@link SeedIds}），并配合 {@code scripts/test-db.sh} 一会话一库。
 *
 * <p>测试库不需要真实账号：读取只要求"已认证"，因此令牌用 {@code AuthPort} 直接签发。
 * 这也顺带证明了一件事——**读路径不依赖账号数据**（本模块没有 account 依赖）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "PAIDEIA_TEST_DB_PASSWORD", matches = ".+")
class KnowledgeReadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthPort authPort;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SeedIds seeds;

    @BeforeEach
    void resolveSeedIds() {
        seeds = new SeedIds(jdbcTemplate);
    }

    private String learner() {
        return "Bearer " + authPort.issue(new AuthPort.Subject("1", AuthPort.ROLE_LEARNER)).value();
    }

    @Test
    void anonymousIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/knowledge/entries"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void categoryTreeIsNested() throws Exception {
        mockMvc.perform(get("/api/v1/knowledge/categories").header("Authorization", learner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                // 根：后端基础（下有 Java 并发）与架构
                .andExpect(jsonPath("$.data[?(@.slug=='backend-basics')]").exists())
                .andExpect(jsonPath("$.data[?(@.slug=='architecture')]").exists())
                .andExpect(jsonPath("$.data[?(@.slug=='backend-basics')].children[0].slug")
                        .value("java-concurrency"));
    }

    @Test
    void listReturnsPublishedEntriesOnly() throws Exception {
        long dependencyInjection = seeds.entry("什么是依赖注入");
        long threadsAndLocks = seeds.entry("线程与锁的基本认识");
        long modularMonolith = seeds.entry("模块化单体的边界");
        long draft = seeds.entry("虚拟线程的落地注意（草稿）");

        mockMvc.perform(get("/api/v1/knowledge/entries?size=50").header("Authorization", learner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.id==" + dependencyInjection + ")]").exists())
                .andExpect(jsonPath("$.data.items[?(@.id==" + threadsAndLocks + ")]").exists())
                .andExpect(jsonPath("$.data.items[?(@.id==" + modularMonolith + ")]").exists())
                // 草稿在学习者侧任何列表都不该出现
                .andExpect(jsonPath("$.data.items[?(@.id==" + draft + ")]").doesNotExist());
    }

    @Test
    void browsingAParentCategoryIncludesItsDescendants() throws Exception {
        // Java 并发是后端基础的子分类；按父分类过滤必须能拿到子分类下的条目
        long backendBasics = seeds.category("backend-basics");
        long javaConcurrency = seeds.category("java-concurrency");

        mockMvc.perform(get("/api/v1/knowledge/entries?categoryId=" + backendBasics + "&size=50")
                        .header("Authorization", learner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.categoryId==" + javaConcurrency + ")]").exists());
    }

    @Test
    void unknownCategoryYieldsEmptyPageInsteadOfError() throws Exception {
        mockMvc.perform(get("/api/v1/knowledge/entries?categoryId=999999").header("Authorization", learner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0))
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    @Test
    void chineseKeywordSearchHitsThroughFulltextIndex() throws Exception {
        long dependencyInjection = seeds.entry("什么是依赖注入");

        mockMvc.perform(get("/api/v1/knowledge/entries?q=依赖注入").header("Authorization", learner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.id==" + dependencyInjection + ")]").exists());
    }

    @Test
    void singleCharacterQueryStillFindsResultsThroughLikeFallback() throws Exception {
        // ngram 分词长度是 2，"锁" 这个单字查询在全文索引上召回为零，必须靠 LIKE 兜住
        long threadsAndLocks = seeds.entry("线程与锁的基本认识");

        mockMvc.perform(get("/api/v1/knowledge/entries?q=锁").header("Authorization", learner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.id==" + threadsAndLocks + ")]").exists());
    }

    @Test
    void searchNeverReturnsDrafts() throws Exception {
        // 关键词必须是**只出现在草稿里**的：ngram 按 bigram 切片，"虚拟线程"的 bigram 里有"线程"，
        // 而"线程与锁"的正文也含"线程"，用它做断言测不出状态过滤（会命中那条而不是草稿）。
        // sql 里写死 status = 'published'，因此草稿永远不会出现在结果里。
        mockMvc.perform(get("/api/v1/knowledge/entries?q=草稿").header("Authorization", learner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void likeWildcardsInQueryAreTreatedLiterally() throws Exception {
        // "%" 若被当成通配符会匹配全部条目；转义之后应当一条也匹配不到。
        //
        // **必须用 `param("q", "%")` 而不是把 `%25` 写进 URL**（2026-10-18 修）：MockMvc 不经过
        // 容器的 URI 解码，`?q=%25` 到了服务端是**字面的三个字符** `%25`——它长度 ≥ 2，于是走的是
        // **全文检索**而不是 LIKE 分支，这条用例此前一直在测一个它没打算测的路径（种子一多就红）。
        mockMvc.perform(get("/api/v1/knowledge/entries").param("q", "%")
                        .header("Authorization", learner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void entryDetailCarriesBodyCategoryAndRelations() throws Exception {
        // 种子关系：依赖注入 → 模块化单体，类型 prerequisite（前者是后者的前置）。
        // 因此后者的前置有前者，而前者的反向链接是后者——两个方向都断言：
        // 入边/出边很容易写反，写反之后两个方向各自都返回"看起来合理"的结果。
        long dependencyInjection = seeds.entry("什么是依赖注入");
        long modularMonolith = seeds.entry("模块化单体的边界");
        long threadsAndLocks = seeds.entry("线程与锁的基本认识");

        mockMvc.perform(get("/api/v1/knowledge/entries/" + modularMonolith)
                        .header("Authorization", learner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(modularMonolith))
                .andExpect(jsonPath("$.data.title").value("模块化单体的边界"))
                .andExpect(jsonPath("$.data.categoryName").value("架构"))
                .andExpect(jsonPath("$.data.body").isNotEmpty())
                .andExpect(jsonPath("$.data.prerequisites[?(@.id==" + dependencyInjection + ")]").exists())
                .andExpect(jsonPath("$.data.dependents").isEmpty())
                // "线程与锁" → "模块化单体" 是 related（无向），因此后者的相关内容里有前者
                .andExpect(jsonPath("$.data.related[?(@.id==" + threadsAndLocks + ")]").exists());

        mockMvc.perform(get("/api/v1/knowledge/entries/" + dependencyInjection)
                        .header("Authorization", learner()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prerequisites").isEmpty())
                .andExpect(jsonPath("$.data.dependents[?(@.id==" + modularMonolith + ")]").exists());
    }

    @Test
    void draftDetailIsNotFound() throws Exception {
        // 草稿对学习者按"不存在"处理：既看不到，也问不出"这里是否有个未发布的条目"
        long draft = seeds.entry("虚拟线程的落地注意（草稿）");

        mockMvc.perform(get("/api/v1/knowledge/entries/" + draft).header("Authorization", learner()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(40400));
    }

    @Test
    void missingEntryIsNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/knowledge/entries/999999").header("Authorization", learner()))
                .andExpect(status().isNotFound());
    }
}
