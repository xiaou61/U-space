package io.github.xiaou61.ai.internal.digest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;

/**
 * 抓取用的 HTTP 薄封装（WORK-027）。
 *
 * <p>存在的理由只有一个：**把三条 SSRF 边界集中在一处**，不让每个源各自实现一遍
 * （各自实现必然有一处漏掉）。三条见 {@link DigestSource} 的类注释。
 *
 * <p><b>为什么不用 AgentScope 的 {@code GuardedHttpTransport}</b>：那是模型客户端的出站闸，
 * 拦的是"上游 baseUrl 被改"这类风险；抓 RSS 是 JDK HttpClient，与它不在一层。
 * WORK-025 的 `NowcoderEssaySource` 就是直接用 JDK 客户端，本项照那条路。
 *
 * <p>**重定向一律不跟随**：源地址来自配置（不是用户输入），风险低于模型 baseUrl，
 * 但仍不该让一次 302 把请求带去别处。宁可这条源失败也不悄悄换主机。
 *
 * <p>不是 final：单测要继承它来替换 `getTextWithHeaders`，验证"主通路失败是否真的走兜底"
 * （2026-10-21：桩掉得靠子类，否则只能起真 HTTP）。
 */
class DigestHttp {

    private final HttpClient client;
    private final Duration timeout;

    DigestHttp(int timeoutSeconds) {
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.min(timeoutSeconds, 10)))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /**
     * GET 一段文本。
     *
     * @throws DigestSource.DigestSourceException 地址非法、非 2xx、超时或网络失败
     */
    String getText(String url, String userAgent) {
        return getTextWithHeaders(url, userAgent, java.util.Map.of());
    }

    /**
     * GET 一段文本，带额外请求头（GitHub Search API 要 accept 与 authorization）。
     *
     * @throws DigestSource.DigestSourceException 地址非法、非 2xx、超时或网络失败
     */
    String getTextWithHeaders(String url, String userAgent, java.util.Map<String, String> headers) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException exception) {
            throw new DigestSource.DigestSourceException("地址非法：" + url, exception);
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new DigestSource.DigestSourceException("只允许 http/https：" + url);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(timeout)
                .header("user-agent", userAgent)
                .header("accept", "*/*")
                .GET();
        headers.forEach(builder::header);
        HttpRequest request = builder.build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                // 3xx 也会落到这里：不跟随重定向，直接按失败处理
                throw new DigestSource.DigestSourceException("HTTP " + status + "：" + url);
            }
            return response.body();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new DigestSource.DigestSourceException("抓取被中断：" + url, exception);
        } catch (java.io.IOException exception) {
            throw new DigestSource.DigestSourceException(
                    "抓取失败：" + url + "（" + exception.getClass().getSimpleName() + "）", exception);
        }
    }
}
