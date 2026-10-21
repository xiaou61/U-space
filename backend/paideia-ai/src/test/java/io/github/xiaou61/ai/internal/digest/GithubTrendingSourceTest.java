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
 * <p><b>2026-10-21 实跑后主次反转</b>：`github.com/trending` 已改成 SPA，服务端 HTML 里
 * 没有仓库数据（实测 `<article>`/`Box-row`/`stars today` 各 0 次）。所以 Search API 是主通路，
 * 页面解析降为补充。这组用例把新顺序钉住。
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

    @Test
    @DisplayName("有 token：Search API 是主通路，成功就不再碰页面")
    void withTokenPrefersSearchApi() {
        String searchJson = """
                {"items":[
                  {"full_name":"owner/repo","html_url":"https://github.com/owner/repo",
                   "description":"a project","language":"Java","stargazers_count":1234}
                ]}
                """;
        StubHttp http = new StubHttp(url -> {
            if (url.contains("api.github.com")) {
                return searchJson;
            }
            throw new DigestSource.DigestSourceException("不该走到页面解析");
        });

        List<FetchedItem> items = source.fetch(properties("ghp_x"), http, 1_800_000_000_000L);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).heat()).isEqualTo(1234);
        assertThat(items.get(0).groupKey()).isEqualTo("github");
        assertThat(http.requested).allMatch(url -> url.contains("api.github.com"));
    }

    @Test
    @DisplayName("**无 token 时也先走 Search API**（页面已是 SPA，不能当主通路）")
    void withoutTokenStillUsesSearchApiFirst() {
        StubHttp http = new StubHttp(url -> {
            if (url.contains("api.github.com")) {
                return """
                        {"items":[{"full_name":"anon/repo","html_url":"https://github.com/anon/repo",
                                   "stargazers_count":9}]}
                        """;
            }
            throw new DigestSource.DigestSourceException("不该走到页面解析");
        });

        List<FetchedItem> items = source.fetch(properties(""), http, 1_800_000_000_000L);

        assertThat(items).hasSize(1);
        assertThat(http.requested).as("只请求了 API").hasSize(1);
        assertThat(http.requested.get(0)).contains("api.github.com");
    }

    @Test
    @DisplayName("Search API 空结果：退回页面解析")
    void searchEmptyFallsBackToPage() {
        String page = """
                <article class="Box-row">
                  <h2 class="h3 lh-condensed"><a href="/trending-owner/hot-repo">hot-repo</a></h2>
                  <p class="col-9 d-inline-block">The hot project description</p>
                  <span class="d-inline-block float-sm-right">3,856 stars today</span>
                  <span itemprop="programmingLanguage">Python</span>
                </article>
                """;
        StubHttp http = new StubHttp(url -> {
            if (url.contains("api.github.com")) {
                return "{\"items\":[]}";
            }
            return page;
        });

        List<FetchedItem> items = source.fetch(properties("ghp_x"), http, 1_800_000_000_000L);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).title()).isEqualTo("trending-owner/hot-repo");
        assertThat(items.get(0).heat()).as("heat 来自当日新增 star").isEqualTo(3856);
        assertThat(items.get(0).meta()).isEqualTo("Python");
        assertThat(http.requested.get(0)).contains("api.github.com");
        assertThat(http.requested.get(1)).contains("github.com/trending");
    }

    @Test
    @DisplayName("**页面是 SPA（没有 article 块）时返回空列表，不抛异常**")
    void spaPageYieldsEmptyWithoutThrowing() {
        // 这是 2026-10-21 实测的真实形态：title 是 Trending，但 HTML 里没有任何仓库数据
        String spa = """
                <html><head><title>Trending repositories on GitHub today</title></head>
                <body><nav>Navigation Menu</nav><main></main></body></html>
                """;
        StubHttp http = new StubHttp(url -> {
            if (url.contains("github.com/trending")) {
                return spa;
            }
            return "{\"items\":[]}";
        });

        List<FetchedItem> items = source.fetch(properties(""), http, 1_800_000_000_000L);

        assertThat(items).isEmpty();
        // 两条通路都试过了
        assertThat(http.requested).anyMatch(url -> url.contains("api.github.com"));
        assertThat(http.requested).anyMatch(url -> url.contains("github.com/trending"));
    }

    @Test
    @DisplayName("两条通路都坏时返回空列表，不抛异常")
    void bothPathsBrokenReturnsEmpty() {
        StubHttp http = new StubHttp(url -> {
            throw new DigestSource.DigestSourceException("网络不通");
        });

        List<FetchedItem> items = source.fetch(properties("ghp_x"), http, 1_800_000_000_000L);

        assertThat(items).isEmpty();
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
