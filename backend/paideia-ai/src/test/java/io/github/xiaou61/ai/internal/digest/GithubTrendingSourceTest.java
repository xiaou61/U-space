package io.github.xiaou61.ai.internal.digest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * GitHub 热榜的通路与兜底顺序（WORK-027 TASK-004）。
 *
 * <p><b>2026-10-22 复测后改成页面优先</b>：部署机上 `github.com/trending?since=daily`
 * 是服务端渲染（12 行、带 stars today），拿到的是真正的今日热榜；而 Search API 的
 * "一周内新建"语义会让同一批仓库连续几天重复，线上因此只剩 2 条。这组用例把新顺序钉住。
 */
class GithubTrendingSourceTest {

    private final GithubTrendingSource source = new GithubTrendingSource();

    /** 记录被请求过的 URL，并按预设返回内容或抛异常。 */
    private static class StubHttp extends DigestHttp {

        final List<String> requested = new ArrayList<>();
        private final java.util.function.Function<String, String> responder;

        StubHttp(java.util.function.Function<String, String> responder) {
            super(5);
            this.responder = responder;
        }

        @Override
        String getTextWithHeaders(String url, String userAgent, Map<String, String> headers) {
            requested.add(url);
            return responder.apply(url);
        }
    }

    private static DigestProperties properties(String token) {
        DigestProperties properties = new DigestProperties();
        properties.setGithubToken(token);
        return properties;
    }

    private static final String PAGE = """
            <article class="Box-row">
              <h2 class="h3 lh-condensed"><a href="/trending-owner/hot-repo">hot-repo</a></h2>
              <p class="col-9 d-inline-block">The hot project description</p>
              <span class="d-inline-block float-sm-right">3,856 stars today</span>
              <span itemprop="programmingLanguage">Python</span>
            </article>
            """;

    @Test
    @DisplayName("有 token 也先走页面：拿到的是真正的今日热榜")
    void prefersTrendingPageEvenWithToken() {
        StubHttp http = new StubHttp(url -> {
            if (url.contains("github.com/trending")) {
                return PAGE;
            }
            throw new DigestSource.DigestSourceException("不该走到 Search API");
        });

        List<FetchedItem> items = source.fetch(properties("ghp_x"), http, 1_800_000_000_000L);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).title()).isEqualTo("trending-owner/hot-repo");
        assertThat(items.get(0).heat()).as("heat 来自当日新增 star").isEqualTo(3856);
        assertThat(items.get(0).meta()).isEqualTo("Python");
        assertThat(items.get(0).groupKey()).isEqualTo("github");
        assertThat(http.requested).allMatch(url -> url.contains("github.com/trending"));
    }

    @Test
    @DisplayName("页面没有 article 块（改版/被拦）：退回 Search API")
    void emptyPageFallsBackToSearchApi() {
        String spa = """
                <html><head><title>Trending repositories on GitHub today</title></head>
                <body><main></main></body></html>
                """;
        StubHttp http = new StubHttp(url -> {
            if (url.contains("github.com/trending")) {
                return spa;
            }
            return """
                    {"items":[{"full_name":"owner/repo","html_url":"https://github.com/owner/repo",
                               "description":"a project","language":"Java","stargazers_count":1234}]}
                    """;
        });

        List<FetchedItem> items = source.fetch(properties(""), http, 1_800_000_000_000L);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).heat()).isEqualTo(1234);
        assertThat(http.requested.get(0)).contains("github.com/trending");
        assertThat(http.requested.get(1)).contains("api.github.com");
    }

    @Test
    @DisplayName("两条通路都坏时返回空列表，不抛异常")
    void bothPathsBrokenReturnsEmpty() {
        StubHttp http = new StubHttp(url -> {
            throw new DigestSource.DigestSourceException("网络不通");
        });

        List<FetchedItem> items = source.fetch(properties("ghp_x"), http, 1_800_000_000_000L);

        assertThat(items).isEmpty();
        assertThat(http.requested).anyMatch(url -> url.contains("github.com/trending"));
        assertThat(http.requested).anyMatch(url -> url.contains("api.github.com"));
    }

    @Test
    @DisplayName("页面里的赞助商与非仓库链接不会被当成热榜条目")
    void ignoresNonRepoLinks() {
        String page = """
                <article class="Box-row sponsor">
                  <a href="/sponsors/someone">Sponsor</a>
                  <a href="/topics/java">Java topic</a>
                </article>
                <article class="Box-row">
                  <h2><a href="/real/repo">real/repo</a></h2>
                  <span>7 stars today</span>
                </article>
                """;
        StubHttp http = new StubHttp(url -> {
            if (url.contains("github.com/trending")) {
                return page;
            }
            return "{\"items\":[]}";
        });

        List<FetchedItem> items = source.fetch(properties(""), http, 1_800_000_000_000L);

        assertThat(items).extracting(FetchedItem::title).containsExactly("real/repo");
    }
}