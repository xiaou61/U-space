package io.github.xiaou61.security;

import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Supplier;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * 只允许来自本机（回环）的请求通过（WORK-028）。
 *
 * <p>用途：把 {@code /actuator/prometheus} 这类"机器读、机器用"的端点锁在回环，
 * 而不必为此另开一个管理端口——**实测**有六处依赖 8081 上的 {@code /actuator/health}
 * （管理端健康徽标、两端 e2e、两个 vite 代理、release.ps1），搬端口会一次性打断它们。
 *
 * <p>判据是 TCP 对端地址而不是 {@code X-Forwarded-For}：后者可伪造。代价与触发条件写在
 * {@code SecurityConfiguration} 的规则注释里——**前面一旦挂反向代理，这条路就失效**。
 */
enum LocalOnlyAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    INSTANCE;

    @Override
    public AuthorizationDecision authorize(
            Supplier<? extends Authentication> authentication, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        return new AuthorizationDecision(isLoopback(request.getRemoteAddr()));
    }

    static boolean isLoopback(String remote) {
        if (remote == null) {
            return false;
        }
        return remote.equals("127.0.0.1")
                || remote.equals("::1")
                || remote.equals("0:0:0:0:0:0:0:1")
                || remote.startsWith("127.");
    }
}
