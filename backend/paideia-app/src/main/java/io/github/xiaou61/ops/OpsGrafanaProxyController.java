package io.github.xiaou61.ops;

import io.github.xiaou61.platform.ApiAccess;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 把管理端对 Grafana 的请求转发到本机回环地址（WORK-028 I-07）。
 *
 * <p><b>为什么不是给 Grafana 开一个公网端口</b>：那要多一个攻击面、多一套账号口令，
 * 还要在云控制台加安全组规则。这里让 Grafana 只监听回环，由应用按 ADMIN 鉴权后转发，
 * 于是 Grafana 的**唯一入口**就是这条已鉴权路径——它内部可以开匿名 Viewer，
 * iframe 里也不会出现第二次登录。
 *
 * <p><b>不转发 `Authorization`</b>：Grafana 不需要它（匿名 Viewer），转发只会让平台令牌
 * 落进另一个服务的访问日志。
 *
 * <p>已知限制：Grafana Live（`/api/live/ws`）需要 WebSocket 升级，本转发器不处理，
 * 面板因此用固定间隔自动刷新。触发升级：确实需要秒级推送时。
 */
@ApiAccess(ApiAccess.Access.ADMIN)
@RestController
class OpsGrafanaProxyController {

    static final String PREFIX = "/api/v1/ops/grafana";

    private final OpsProperties properties;
    private final HttpClient http;

    OpsGrafanaProxyController(OpsProperties properties) {
        this.properties = properties;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getQueryTimeoutMs()))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    /** 页面、静态资源、数据源查询的 GET 部分。 */
    @GetMapping(PREFIX + "/**")
    ResponseEntity<byte[]> get(HttpServletRequest request) throws IOException, InterruptedException {
        return forward(request, HttpMethod.GET, null);
    }

    /**
     * Grafana 的查询接口（{@code /api/ds/query}）**只接受 POST**，面板的每条曲线都走它。
     *
     * <p>这是 REQ-036「仅 GET」的唯一例外，且语义仍是只读查询：**本控制器不保存任何东西**，
     * 只是把请求体转给 Grafana。真正的写操作（改 dashboard、改数据源）仍被 Grafana 的
     * Viewer 角色与 provisioning 只读模式挡住。
     */
    @PostMapping(PREFIX + "/**")
    ResponseEntity<byte[]> post(HttpServletRequest request, @RequestBody(required = false) byte[] body)
            throws IOException, InterruptedException {
        return forward(request, HttpMethod.POST, body);
    }

    private ResponseEntity<byte[]> forward(HttpServletRequest request, HttpMethod method, byte[] body)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(targetUri(request)))
                .timeout(Duration.ofMillis(Math.max(properties.getQueryTimeoutMs(), 5000)));
        copyHeader(request, builder, HttpHeaders.CONTENT_TYPE);
        copyHeader(request, builder, HttpHeaders.ACCEPT);
        if (method == HttpMethod.GET) {
            builder.GET();
        } else {
            builder.method(method.name(), body == null
                    ? HttpRequest.BodyPublishers.noBody()
                    : HttpRequest.BodyPublishers.ofByteArray(body));
        }

        HttpResponse<byte[]> response;
        try {
            response = http.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        } catch (IOException e) {
            // Grafana 不在（未启动、端口被占、刚重启）时**必须是 502 而不是 500**：
            // 500 会被读成"应用自己坏了"，而这明明是上游不可达。摘要卡片不依赖 Grafana，
            // 所以这一块失败时页面的其余部分照常可用。
            String message = "Grafana 不可达（" + properties.getGrafanaUrl() + "）：" + e.getMessage();
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        HttpHeaders headers = new HttpHeaders();
        String contentType = response.headers().firstValue(HttpHeaders.CONTENT_TYPE).orElse(null);
        if (contentType != null) {
            headers.setContentType(MediaType.parseMediaType(contentType));
        }
        // **重定向必须改写成站内相对路径**：Grafana 按 `root_url` 生成绝对地址，
        // 那是 http://127.0.0.1:3000/api/v1/ops/grafana/...，浏览器根本连不到回环。
        // 实测表现为面板 301 到一个不可达地址、页面一片空白。
        String location = response.headers().firstValue(HttpHeaders.LOCATION).orElse(null);
        if (location != null) {
            headers.set(HttpHeaders.LOCATION, rewriteLocation(location));
        }
        return new ResponseEntity<>(response.body(), headers, HttpStatus.valueOf(response.statusCode()));
    }

    /** 把指向回环 Grafana 的绝对地址换成站内相对路径；其它地址原样保留。 */
    String rewriteLocation(String location) {
        int index = location.indexOf(PREFIX);
        if (index < 0) {
            return location;
        }
        return location.substring(index);
    }

    /** 去掉前缀、保留查询串。单独提出来是为了能逐条断言拼接结果。 */
    static String targetUri(String baseUrl, String requestUri, String queryString) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = requestUri.length() > PREFIX.length() ? requestUri.substring(PREFIX.length()) : "/";
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return queryString == null || queryString.isBlank() ? base + path : base + path + "?" + queryString;
    }

    private String targetUri(HttpServletRequest request) {
        return targetUri(properties.getGrafanaUrl(), request.getRequestURI(), request.getQueryString());
    }

    private static void copyHeader(HttpServletRequest request, HttpRequest.Builder builder, String name) {
        String value = request.getHeader(name);
        if (value != null && !value.isBlank()) {
            builder.header(name, value);
        }
    }
}
