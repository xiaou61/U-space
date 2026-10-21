package io.github.xiaou61.ai.internal.digest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * GitHub 每日热榜（WORK-027）。
 *
 * <p><b>双通路，主次随 token 反转</b>（research.md 实测依据）：
 * <ul>
 *   <li><b>有 token → Search API 为主</b>：`search/repositories?q=created:>=<7天前> stars:>100&sort=stars`，
 *       稳定、限 30 次/分（带 token）。语义偏：量的是「新建且涨得快」，不等于官方 Trending。</li>
 *   <li><b>无 token → 页面解析为主</b>：`github.com/trending?since=daily`，匿名限 60 次/时。
 *       本机三次连接被拒、生产 200 且 12 行可解析——所以它是「能拿到更准」但「不稳」的那条。</li>
 * </ul>
 * **任何通路失败都要留记录**（REQ-003），不静默整组为空；两条都坏时返回空列表并由编排层记日志。
 *
 * <p><b>页面解析要注意的坑</b>：不能直接匹配 `href="/([^"]+)"`——会撞到赞助商链接。
 * 必须先定位 `<article class="Box-row">` 块，在块内取 `<h2>` 里的链接。
 */
@Component
class GithubTrendingSource {

    private static final Logger log = LoggerFactory.getLogger(GithubTrendingSource.class);

    private static final String TRENDING_URL = "https://github.com/trending?since=daily";

    private static final String SEARCH_URL =
            "https://api.github.com/search/repositories?q=%s&sort=stars&per_page=15";

    private static final String USER_AGENT = "PaideiaDigest/1.0";

    /** 前端分组。 */
    static final String GROUP = "github";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 抓热榜。
     *
     * <p><b>主次在 2026-10-21 实跑后反转了</b>：原设计是「有 token 走 Search API，无 token 走页面解析」，
     * 但实测发现 **`github.com/trending` 已经改成 SPA**——服务端返回的 HTML 里一个仓库链接都没有
     * （`title` 是 Trending，但 `<article>`/`Box-row`/`stars today` 全为 0 次，仓库行由 JS 注入）。
     * 也就是说"页面解析"这条路**在当前页面结构下永不命中**，把它放在无 token 时的主通路等于
     * 每天拿一次空结果。
     *
     * <p>所以改为：**Search API 永远是主通路**（有 token 时限额高，无 token 时 60/时，一天一次够用），
     * 页面解析保留为"万一 GitHub 把服务端渲染改回来"的补充——它先试，命中就多一条更准的数据源。
     *
     * <p>**两条都失败返回空列表**（不抛异常）：这一组空掉，其它组照常。
     */
    List<FetchedItem> fetch(DigestProperties properties, DigestHttp http, long nowMillis) {
        String token = properties.getGithubToken();
        boolean hasToken = token != null && !token.isBlank();

        // 主通路：Search API
        List<FetchedItem> viaSearch = searchApi(hasToken ? token : "", http, nowMillis);
        if (!viaSearch.isEmpty()) {
            return viaSearch;
        }

        // 补充通路：页面解析（当前是 SPA，正常不会命中；保留以防 GitHub 改回服务端渲染）
        List<FetchedItem> viaPage = trendingPage(http, nowMillis);
        if (!viaPage.isEmpty()) {
            log.info("GitHub Search API 无结果，页面解析拿到 {} 条", viaPage.size());
            return viaPage;
        }
        log.warn("GitHub 热榜两条通路都没有结果（Search API 与页面解析），这一组本次为空");
        return List.of();
    }

    /**
     * 走 Search API。任何异常都返回空列表，由上层决定兜底。
     *
     * <p><b>查询串必须整体 URL 编码</b>：`created:>=2026-10-14 stars:>100` 里的 `>` 是 URI 的
     * 非法字符，{@code URI.create} 会直接抛 {@code IllegalArgumentException}（2026-10-21 实测：
     * 这条通路静默返回空，而同一串用 curl 手打是正常的——只有实跑才暴露）。
     */
    private List<FetchedItem> searchApi(String token, DigestHttp http, long nowMillis) {
        // 7 天前：GitHub 的 search 语法用日期而非时间戳
        String since = java.time.Instant.ofEpochMilli(nowMillis)
                .atZone(java.time.ZoneOffset.UTC).toLocalDate().minusDays(7).toString();
        String query = java.net.URLEncoder.encode("created:>=" + since + " stars:>100",
                java.nio.charset.StandardCharsets.UTF_8);
        String url = SEARCH_URL.formatted(query);
        try {
            String body = http.getTextWithHeaders(url, USER_AGENT, githubHeaders(token));
            JsonNode items = objectMapper.readTree(body).path("items");
            List<FetchedItem> result = new ArrayList<>();
            for (JsonNode item : items) {
                String fullName = item.path("full_name").asText("");
                String htmlUrl = item.path("html_url").asText("");
                if (htmlUrl.isBlank()) {
                    continue;
                }
                String description = item.path("description").asText("");
                String language = item.path("language").asText("");
                int stars = item.path("stargazers_count").asInt(0);
                result.add(new FetchedItem(
                        fullName,
                        htmlUrl,
                        truncate(description),
                        stars,
                        language.isBlank() ? null : language,
                        nowMillis,
                        "github-trending",
                        "GitHub 热榜",
                        GROUP));
            }
            return result;
        } catch (DigestSource.DigestSourceException exception) {
            log.warn("GitHub Search API 失败：{}", exception.getMessage());
            return List.of();
        } catch (Exception exception) {
            log.warn("GitHub Search API 解析失败：{}", exception.getClass().getSimpleName());
            return List.of();
        }
    }

    /**
     * 解析 trending 页面。
     *
     * <p>**先在 `<article` 边界里切块**，再在块内找第一个 `/owner/repo` 链接。
     * 直接全文匹配 href 会抓到赞助商与导航链接——这是 research.md 记录过的坑。
     */
    List<FetchedItem> trendingPage(DigestHttp http, long nowMillis) {
        String html;
        try {
            html = http.getText(TRENDING_URL, USER_AGENT);
        } catch (DigestSource.DigestSourceException exception) {
            log.warn("GitHub trending 页面不可达：{}", exception.getMessage());
            return List.of();
        }
        List<FetchedItem> result = new ArrayList<>();
        // 按 <article 切块：每个块要么是一个仓库行，要么是别的公告
        String[] blocks = html.split("<article");
        for (String block : blocks) {
            String repo = firstRepoLink(block);
            if (repo == null) {
                continue;
            }
            String description = firstByAttribute(block, "col-9");
            String stars = firstStarsToday(block);
            String language = firstByItemprop(block, "programmingLanguage");
            result.add(new FetchedItem(
                    repo,
                    "https://github.com/" + repo,
                    truncate(stripTags(description)),
                    parseStars(stars),
                    language,
                    nowMillis,
                    "github-trending",
                    "GitHub 热榜",
                    GROUP));
        }
        return result;
    }

    /**
     * 块内第一个看起来像 `owner/repo` 的链接。
     *
     * <p>排除 `/topics/`、`/sponsors/`、`/login` 这些非仓库路径——trending 页确实会带它们。
     */
    private static String firstRepoLink(String block) {
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("href=\"/(?!topics/|sponsors/|login|features/|marketplace)"
                        + "([A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+)\"").matcher(block);
        return matcher.find() ? matcher.group(1) : null;
    }

    /** 取带某个 class 的元素的文本（用来捞描述）。刻意宽松：GitHub 改版时会加类名。 */
    private static String firstByAttribute(String block, String className) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("<p[^>]*class=\"[^\"]*" + className + "[^\"]*\"[^>]*>(.*?)</p>",
                        java.util.regex.Pattern.DOTALL)
                .matcher(block);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String firstByItemprop(String block, String itemprop) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("itemprop=\"" + itemprop + "\"[^>]*>\\s*([^<]+)", java.util.regex.Pattern.DOTALL)
                .matcher(block);
        return matcher.find() ? matcher.group(1).strip() : null;
    }

    private static String firstStarsToday(String block) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("([\\d,]+)\\s*stars?\\s*today", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(block);
        return matcher.find() ? matcher.group(1) : null;
    }

    /** `3,856` → 3856。解析不了返回 null（heat 是可选字段）。 */
    private static Integer parseStars(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(raw.replace(",", "").replace(" ", ""));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static String stripTags(String html) {
        return html == null ? "" : html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").strip();
    }

    private static String truncate(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String value = stripTags(raw);
        return value.length() <= 200 ? value : value.substring(0, 200);
    }

    private static java.util.Map<String, String> githubHeaders(String token) {
        java.util.Map<String, String> headers = new java.util.LinkedHashMap<>();
        headers.put("accept", "application/vnd.github+json");
        if (token != null && !token.isBlank()) {
            headers.put("authorization", "Bearer " + token);
        }
        return headers;
    }
}
