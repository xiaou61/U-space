package io.github.xiaou61.account.internal.oauth;

import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import io.github.xiaou61.security.CurrentUser;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账号的第三方绑定（WORK-037）。
 *
 * <p>挂在 {@code /api/v1/account/**} 之下，因此**要求已登录**（落到
 * {@code anyRequest().authenticated()}），与 {@code /auth/oauth/**} 的放行前缀刻意分开。
 *
 * <p><b>归属一律取自令牌</b>：路径与请求体里都没有用户标识。允许传 userId 就等于
 * 允许给别人的账号绑一个自己控制的第三方账号——那是账号接管的最短路径。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/account/oauth")
class OAuthIdentityController {

    private final OAuthLoginService loginService;
    private final OAuthProperties properties;
    private final OAuthStateStore states;

    OAuthIdentityController(OAuthLoginService loginService, OAuthProperties properties,
                            OAuthStateStore states) {
        this.loginService = loginService;
        this.properties = properties;
        this.states = states;
    }

    /**
     * 已绑定的登录方式，以及"有没有密码"。
     *
     * <p>{@code providerUserId} 不下发：界面上没有用处，泄露出去反而方便别人拼出
     * "这个人绑的是哪个平台账号"。
     */
    @GetMapping("/identities")
    ApiResponse<IdentitiesView> identities() {
        long userId = currentUserId();
        OAuthLoginService.Bindings bindings = loginService.bindingsOf(userId);
        List<BoundView> bound = bindings.identities().stream()
                .map(BoundView::of)
                .toList();
        List<ProviderView> available = java.util.Arrays.stream(OAuthProvider.values())
                .filter(properties::enabled)
                .map(provider -> new ProviderView(provider.key(), provider.label()))
                .toList();
        return ApiResponse.ok(new IdentitiesView(bound, bindings.hasPassword(), available));
    }

    /**
     * 发起绑定：与登录走同一个回调，但 state 里记着"这是绑定"与"绑给谁"。
     *
     * <p>已登录状态下的 start **不需要**再校验当前是否已绑过这个平台：解绑后重绑是正常操作，
     * 而重复绑定在服务层是幂等的。
     */
    @PostMapping("/{provider}/start")
    ApiResponse<OAuthController.StartResponse> startBinding(@PathVariable String provider) {
        long userId = currentUserId();
        OAuthProvider resolved = OAuthProvider.ofKey(provider);
        if (resolved == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "不支持的登录方式");
        }
        if (!properties.enabled(resolved)) {
            throw new BizException(ErrorCode.CONFLICT, "该登录方式尚未启用");
        }
        String state = states.issue(resolved, OAuthPurpose.BIND, userId);
        String url = resolved.authorizeUrl(
                properties.provider(resolved).clientId(),
                properties.redirectUri(resolved),
                state);
        return ApiResponse.ok(new OAuthController.StartResponse(url));
    }

    /**
     * 解绑。
     *
     * @throws BizException 解完就没有任何一种登录方式了
     */
    @DeleteMapping("/{provider}")
    ApiResponse<Void> unbind(@PathVariable String provider) {
        long userId = currentUserId();
        OAuthProvider resolved = OAuthProvider.ofKey(provider);
        if (resolved == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "不支持的登录方式");
        }
        loginService.unbind(userId, resolved);
        return ApiResponse.ok(null);
    }

    private static long currentUserId() {
        String subject = CurrentUser.subjectId();
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException exception) {
            // 令牌里的 sub 不是数字，说明签发侧与这里不一致；按未认证处理，不猜
            throw new BizException(ErrorCode.UNAUTHENTICATED);
        }
    }

    record ProviderView(String key, String label) {
    }

    record BoundView(String provider, String label, String displayName, Instant boundAt) {

        static BoundView of(UserIdentity identity) {
            OAuthProvider provider = OAuthProvider.ofKey(identity.getProvider());
            return new BoundView(
                    identity.getProvider(),
                    provider == null ? identity.getProvider() : provider.label(),
                    identity.getDisplayName(),
                    identity.getCreatedAt());
        }
    }

    record IdentitiesView(List<BoundView> bound, boolean hasPassword, List<ProviderView> available) {
    }
}
