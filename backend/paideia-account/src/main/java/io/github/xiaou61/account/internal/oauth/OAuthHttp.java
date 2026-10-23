package io.github.xiaou61.account.internal.oauth;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 与平台之间的两次 HTTP 调用：换令牌、取用户信息。
 *
 * <p><b>它不是什么</b>：不是通用代理。地址只来自 {@link OAuthProvider} 里写死的常量，
 * 因此不需要 SSRF 守卫（用户输入不参与拼地址）。**不要**给它加"传入任意 URL"的重载——
 * 那会把这里变成一个能从外网打进内网的跳板。
 *
 * <p><b>不跟随重定向</b>：GitHub 与 Gitee 在这两个端点上都不该 302。跟随的话，
 * 一次响应头就能把携带令牌的请求带去别处。
 *
 * <p><b>令牌不进日志</b>：这里只记平台与结果。授权码与 access token 都是一次性凭据，
 * 但写进日志就等于把它们复制到了另一个可长期读取的地方。
 */
@Component
public class OAuthHttp {

    private static final Logger log = LoggerFactory.getLogger(OAuthHttp.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    private final ObjectMapper json = new ObjectMapper();

    /**
     * 用授权码换平台 access token。
     *
     * <p>两个平台都是表单 POST，但 GitHub 默认返回 URL 编码的响应，必须显式
     * {@code accept: application/json} 才能拿到 JSON——不写这一条，解析会一直失败，
     * 而失败信息看起来像是"平台没返回 access_token"。
     */
    public String exchangeCode(OAuthProvider provider, OAuthProperties.Provider config,
                              String redirectUri, String code) {
        String form = "grant_type=authorization_code"
                + "&client_id=" + encode(config.clientId())
                + "&client_secret=" + encode(config.clientSecret())
                + "&code=" + encode(code)
                + "&redirect_uri=" + encode(redirectUri);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.tokenUrl()))
                .timeout(TIMEOUT)
                .header("accept", "application/json")
                .header("content-type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        JsonNode body = send(provider, request, "换令牌");
        String accessToken = textOf(body, "access_token");
        if (accessToken == null || accessToken.isBlank()) {
            // 平台的错误明细（error / error_description）可能提到账号状态，不回显给用户
            log.warn("第三方换令牌未返回 access_token：provider={} error={}",
                    provider.key(), textOf(body, "error"));
            throw new BizException(ErrorCode.UNAUTHENTICATED, "授权失败，请重试");
        }
        return accessToken;
    }

    /** 用平台 access token 取用户信息，原样返回 JSON，由 {@link OAuthProfile} 归一化。 */
    public JsonNode fetchUser(OAuthProvider provider, String platformToken) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(provider.userUrl()))
                .timeout(TIMEOUT)
                .header("accept", "application/json")
                .header("authorization", "Bearer " + platformToken)
                .GET()
                .build();
        return send(provider, request, "取用户信息");
    }

    private JsonNode send(OAuthProvider provider, HttpRequest request, String action) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                throw new BizException(ErrorCode.UNAUTHENTICATED, "授权失败，请重试");
            }
            return json.readTree(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BizException(ErrorCode.INTERNAL, "授权请求被中断，请重试");
        } catch (java.io.IOException exception) {
            // 网络问题与平台故障对用户是同一件事：重试。日志里留平台与阶段以便定位。
            log.warn("第三方{}失败：provider={}（{}）",
                    action, provider.key(), exception.getClass().getSimpleName());
            throw new BizException(ErrorCode.INTERNAL, "无法连接授权服务，请稍后重试");
        }
    }

    private static String textOf(JsonNode body, String field) {
        return Optional.ofNullable(body.get(field)).map(JsonNode::asText).orElse(null);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
