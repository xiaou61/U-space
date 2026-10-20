package io.github.xiaou61.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

/**
 * 反代前缀额外接受查询串里的令牌（WORK-028）。
 *
 * <p><b>为什么需要</b>：管理端把 Grafana 面板放在同源 iframe 里，而浏览器对 iframe 的
 * **导航请求不会附加 `Authorization` 头**。本项目无 Cookie、全靠这个头，实测 iframe 内的
 * 文档请求被安全链挡成 401，面板一片空白。
 *
 * <p><b>范围刻意最小</b>：只有 `/api/v1/ops/grafana/` 下的请求才看查询串，
 * 其余路径完全沿用 {@link DefaultBearerTokenResolver}（只看头）。这样"令牌出现在 URL 里"
 * 这件事被关在一个已知代价里，而不是扩散到全站。
 *
 * <p><b>已知代价</b>：该前缀的 URL 会带令牌，因此可能落进访问日志与浏览器历史。
 * 它只用于管理员面板，且令牌本身 15 分钟过期。升级触发条件：Grafana 支持在同源反代下
 * 用会话 Cookie 时改回 Cookie。
 */
final class OpsTicketBearerResolver implements BearerTokenResolver {

    static final OpsTicketBearerResolver INSTANCE = new OpsTicketBearerResolver();

    private static final String OPS_PANEL_PREFIX = "/api/v1/ops/grafana";
    private static final String PARAM = "ticket";
    private final BearerTokenResolver standard = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest request) {
        String fromHeader = standard.resolve(request);
        if (fromHeader != null) {
            return fromHeader;
        }
        if (!request.getRequestURI().startsWith(OPS_PANEL_PREFIX)) {
            return null;
        }
        String ticket = request.getParameter(PARAM);
        return ticket == null || ticket.isBlank() ? null : ticket;
    }
}
