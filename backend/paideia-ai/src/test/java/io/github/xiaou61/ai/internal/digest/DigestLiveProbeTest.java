package io.github.xiaou61.ai.internal.digest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * 真实联调：七个源各抓一次，断言都能解析出非零条目（WORK-027 TASK-003/004 的实跑证据）。
 *
 * <p>**默认跳过**（连外网、受对方站点状态影响），用 {@code -Dpaideia.digest.live=true} 打开
 * ——与既有 {@code LayaContentClassifierLiveTest} 同一手法。
 *
 * <p>它的价值不是"回归"，而是**源改版时能立刻看到是哪一家坏了**：
 * 单测喂的是夹具，夹具不会自己坏。
 */
@EnabledIfSystemProperty(named = "paideia.digest.live", matches = "true")
class DigestLiveProbeTest {

    private final FeedXml feedXml = new FeedXml();
    private final DigestHttp http = new DigestHttp(20);

    /** 配置里的五个 RSS 源与实际端点。 */
    private static final List<String[]> RSS = List.of(
            new String[] {"qbitai", "量子位", "https://www.qbitai.com/feed"},
            new String[] {"ithome", "IT之家", "https://www.ithome.com/rss/"},
            new String[] {"infoq", "InfoQ", "https://www.infoq.cn/feed"},
            new String[] {"juejin", "掘金", "https://juejin.cn/rss"},
            new String[] {"oschina", "开源中国", "https://www.oschina.net/news/rss"});

    @Test
    @DisplayName("五个 RSS 源各能解析出非零条目")
    void rssSourcesYieldItems() {
        for (String[] source : RSS) {
            String body = http.getText(source[2], "PaideiaDigest/1.0");
            List<FeedXml.Entry> entries = feedXml.parse(body, System.currentTimeMillis());
            System.out.printf("[live] %-8s 解析出 %d 条，首条：%s%n", source[0], entries.size(),
                    entries.isEmpty() ? "(无)" : entries.get(0).title());
            assertThat(entries)
                    .as("%s（%s）应至少解析出一条——零条说明源改版或解析器需要更新",
                            source[1], source[2])
                    .isNotEmpty();
        }
    }

    @Test
    @DisplayName("页面解析当前返回 0 条（GitHub 已改 SPA）——这条记录的是事实，不是回归")
    void githubTrendingPageIsNowSpa() {
        List<FetchedItem> repos = new GithubTrendingSource()
                .trendingPage(http, System.currentTimeMillis());
        System.out.printf("[live] github trending 页面解析出 %d 条%n", repos.size());
        // 2026-10-21 实测：github.com/trending 的服务端 HTML 里没有仓库数据（仓库行由 JS 注入）。
        // 所以这里**断言为空**——若哪天它非空，说明 GitHub 改回了服务端渲染，
        // 那时应把这个测试改回 isNotEmpty 并重新评估主次通路。
        assertThat(repos)
                .as("页面解析目前拿不到数据（SPA）；若此断言失败说明 GitHub 改回服务端渲染了")
                .isEmpty();
    }

    @Test
    @DisplayName("HN Algolia 返回前 10，points 作为 heat")
    void hackerNewsYieldsHits() {
        List<FetchedItem> hits = new HackerNewsSource().fetch(http, System.currentTimeMillis());
        System.out.printf("[live] hacker news 解析出 %d 条，首条：%s（%s points）%n",
                hits.size(), hits.isEmpty() ? "(无)" : hits.get(0).title(),
                hits.isEmpty() ? "-" : String.valueOf(hits.get(0).heat()));
        assertThat(hits).isNotEmpty();
    }

    @Test
    @DisplayName("GitHub Search API 通路也能返回（有 token 时是主通路）")
    void githubSearchApiWorks() {
        DigestProperties properties = new DigestProperties();
        properties.setGithubToken(System.getProperty("paideia.digest.githubToken", ""));
        List<FetchedItem> items = new GithubTrendingSource()
                .fetch(properties, http, System.currentTimeMillis());
        System.out.printf("[live] GitHub 双通路共 %d 条（token=%s）%n", items.size(),
                properties.getGithubToken().isBlank() ? "无" : "有");
        assertThat(items).as("两条通路至少有一条能用").isNotEmpty();
    }
}
