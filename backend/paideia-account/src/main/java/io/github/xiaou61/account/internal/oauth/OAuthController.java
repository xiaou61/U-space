package io.github.xiaou61.account.internal.oauth;

import io.github.xiaou61.account.internal.token.AuthTokens;
import io.github.xiaou61.account.internal.token.TokenService;
import io.github.xiaou61.account.internal.user.User;
import io.github.xiaou61.account.internal.user.UserMapper;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 第三方登录的匿名端点（WORK-037）。
 *
 * <p>挂在 {@code /api/v1/auth/**} 下，因此**整体放行**（见 SecurityConfiguration 的前缀纪律）。
 * 这三个端点只做"换令牌"的事，不读任何业务数据：
 *
 * <ul>
 *   <li>{@code start}：给一个要跳转的平台地址</li>
 *   <li>{@code callback}：平台跳回来，换令牌并转交</li>
 *   <li>{@code exchange}：前端拿一次性握手码换令牌</li>
 * </ul>
 *
 * <p><b>回调必须能匿名访问</b>：平台跳转时不带我们的 Authorization 头。安全性由 state
 * 承担，而不是由认证承担——这也是 state 必须一次性、服务端校验的原因。
 */
@ApiAccess(ApiAccess.Access.PUBLIC_WRITE)
@RestController
@RequestMapping("/api/v1/auth/oauth")
class OAuthController {

    private static final Logger log = LoggerFactory.getLogger(OAuthController.class);

    private final OAuthProperties properties;
    private final OAuthStateStore states;
    private final HandoffStore handoffs;
    private final OAuthHttp http;
    private final OAuthLoginService loginService;
    private final TokenService tokens;
    private final UserMapper users;

    OAuthController(OAuthProperties properties, OAuthStateStore states, HandoffStore handoffs,
                    OAuthHttp http, OAuthLoginService loginService, TokenService tokens,
                    UserMapper users) {
        this.properties = properties;
        this.states = states;
        this.handoffs = handoffs;
        this.http = http;
        this.loginService = loginService;
        this.tokens = tokens;
        this.users = users;
    }

    /**
     * 有哪些平台可用。登录页据此决定是否渲染按钮——不渲染比渲染出来点了报错好。
     */
    @GetMapping("/providers")
    ApiResponse<ProvidersResponse> providers() {
        java.util.List<ProviderView> enabled = new java.util.ArrayList<>();
        for (OAuthProvider provider : OAuthProvider.values()) {
            if (properties.enabled(provider)) {
                enabled.add(new ProviderView(provider.key(), provider.label()));
            }
        }
        return ApiResponse.ok(new ProvidersResponse(enabled));
    }

    /** 发起登录：返回要跳转的平台地址。 */
    @PostMapping("/{provider}/start")
    ApiResponse<StartResponse> start(@PathVariable String provider,
                                    @RequestParam(required = false, defaultValue = "false") boolean desktop) {
        OAuthProvider resolved = requireProvider(provider);
        // desktop 由请求方声明，但它只影响"成功后回哪儿"，不携带任何权限含义——
        // 冒充它最多让用户在自己浏览器里看到一句"请在应用内完成登录"
        String state = states.issue(resolved, OAuthPurpose.LOGIN, null, desktop);
        String url = resolved.authorizeUrl(
                properties.provider(resolved).clientId(),
                properties.redirectUri(resolved),
                state);
        return ApiResponse.ok(new StartResponse(url));
    }

    /**
     * 平台回调。
     *
     * <p><b>所有失败都以 302 回登录页</b>，而不是返回 JSON 错误：这个端点是被浏览器直接
     * 导航到的，用户看到的应该是一个能继续操作的页面，而不是一段 {"code":40100}。
     *
     * <p>失败原因只给粗粒度码（denied / expired / failed / disabled），不把平台的
     * error_description 转给用户——那里可能提到账号状态或内部细节。
     */
    @GetMapping("/{provider}/callback")
    void callback(@PathVariable String provider,
                  @RequestParam(required = false) String code,
                  @RequestParam(required = false) String state,
                  @RequestParam(required = false) String error,
                  HttpServletRequest request,
                  HttpServletResponse response) throws IOException {
        OAuthProvider resolved = OAuthProvider.ofKey(provider);
        if (resolved == null) {
            redirect(response, errorUrl("failed"));
            return;
        }
        if (!properties.enabled(resolved)) {
            redirect(response, errorUrl("disabled"));
            return;
        }
        if (error != null && !error.isBlank()) {
            // 用户在平台上点了"拒绝"：不是故障，不该按异常记
            redirect(response, errorUrl("denied"));
            return;
        }

        // state 必须先消费再做事：任何分支之下它都不该还能用第二次
        OAuthState pending = states.take(state).orElse(null);
        if (pending == null || pending.provider() != resolved) {
            log.info("第三方回调 state 无效：provider={}", resolved.key());
            redirect(response, errorUrl("expired"));
            return;
        }
        if (code == null || code.isBlank()) {
            redirect(response, errorUrl("failed"));
            return;
        }

        try {
            AuthTokens issued = exchangeAndIssueTokens(resolved, pending, code);
            String handoff = handoffs.issue(Long.parseLong(issued.user().id()));
            // 令牌不进 URL：地址里只有一个 60 秒有效、一次性的握手码
            redirect(response, successUrl(handoff, pending.purpose(), pending.desktop()));
        } catch (BizException exception) {
            log.info("第三方回调失败：provider={} reason={}", resolved.key(), exception.getMessage());
            redirect(response, errorUrl("failed"));
        }
    }

    /** 前端用握手码换令牌。响应结构与 {@code /auth/login} 完全相同。 */
    @PostMapping("/exchange")
    ApiResponse<AuthTokens> exchange(@RequestBody ExchangeRequest body) {
        Long userId = handoffs.consume(body == null ? null : body.handoff())
                .orElseThrow(() -> new BizException(ErrorCode.INVALID_ARGUMENT, "登录链接已失效，请重新登录"));
        User user = users.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHENTICATED, "登录链接已失效，请重新登录"));
        return ApiResponse.ok(tokens.issueFor(user));
    }

    private AuthTokens exchangeAndIssueTokens(OAuthProvider provider, OAuthState pending, String code) {
        OAuthProperties.Provider config = properties.provider(provider);
        String platformToken = http.exchangeCode(provider, config, properties.redirectUri(provider), code);
        OAuthProfile profile = OAuthProfile.from(http.fetchUser(provider, platformToken));

        if (pending.purpose() == OAuthPurpose.BIND) {
            // 绑定意在图里到过回调：此时用户已经登录，令牌早就有了。回调只负责建立绑定，
            // 再签一组新令牌是为了让前端走同一条"握手码"路径，不必在回调页特判。
            if (pending.userId() == null) {
                throw new BizException(ErrorCode.UNAUTHENTICATED, "登录已失效，请重新登录");
            }
            loginService.bind(pending.userId(), provider, profile);
            User user = users.findById(pending.userId())
                    .orElseThrow(() -> new BizException(ErrorCode.UNAUTHENTICATED, "登录已失效，请重新登录"));
            return tokens.issueFor(user);
        }
        return loginService.loginOrRegister(provider, profile);
    }

    private OAuthProvider requireProvider(String raw) {
        OAuthProvider provider = OAuthProvider.ofKey(raw);
        if (provider == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "不支持的登录方式");
        }
        if (!properties.enabled(provider)) {
            throw new BizException(ErrorCode.CONFLICT, "该登录方式尚未启用");
        }
        return provider;
    }

    private void redirect(HttpServletResponse response, String location) throws IOException {
        response.setStatus(HttpStatus.FOUND.value());
        response.setHeader("Location", location);
    }

    private String errorUrl(String reason) {
        return webBase() + "/#/login?oauthError=" + encode(reason);
    }

    /**
     * 成功后的去向。
     *
     * <p>绑定回账号设置页，登录回回调页；桌面壳发起的登录额外带 {@code desktop=1}，
     * 由回调页把握手码转成 {@code paideia://} 交给应用。
     *
     * <p><b>为什么不直接 302 到 {@code paideia://}</b>：浏览器会把"从 http 跳到自定义协议"
     * 当成不安全跳转拦掉，只有在真实页面里执行 {@code location.href} 才放行。
     * 所以必须先回到我们自己的页面，再由页面发起跳转。
     */
    private String successUrl(String handoff, OAuthPurpose purpose, boolean desktop) {
        if (purpose == OAuthPurpose.BIND) {
            return webBase() + "/#/profile?oauthBound=1&handoff=" + encode(handoff);
        }
        String desktopFlag = desktop ? "&desktop=1" : "";
        return webBase() + "/#/oauth/callback?handoff=" + encode(handoff) + desktopFlag;
    }

    private String webBase() {
        String base = properties.webBaseUrl();
        return base == null || base.isBlank() ? "" : base.replaceAll("/+$", "");
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    record ProviderView(String key, String label) {
    }

    record ProvidersResponse(java.util.List<ProviderView> providers) {
    }

    record StartResponse(String authorizeUrl) {
    }

    record ExchangeRequest(String handoff) {
    }
}
