package io.github.xiaou61.account.internal.oauth;

import io.github.xiaou61.account.internal.support.ExpiringStore;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

/**
 * 授权往返的一次性 state（WORK-037）。
 *
 * <p><b>为什么需要它</b>：回调端点是匿名的，平台跳回来时只带 {@code code} 与 {@code state}。
 * 不校验 state，任何人都能构造一个指向我们回调的链接，把自己的平台账号塞进受害者的浏览器，
 * 造成"登录 CSRF"——受害者在自己不知道的情况下登进了攻击者的账号。
 *
 * <p><b>为什么不放 Cookie</b>：本项目无 Cookie、纯 Bearer，桌面壳的页面来源是
 * {@code http://127.0.0.1:5310}，`SameSite`/`Secure` 语义在那种来源上失效。所以 state
 * 走服务端存储，浏览器只需要把它原样带回来。
 *
 * <p><b>天花板与既有三处一致</b>：进程内、多实例部署时必须换共享存储，否则"在我这里发起、
 * 落到另一台回调"会表现为 state 不存在。
 */
public final class OAuthStateStore {

    private static final Duration TTL = OAuthConfiguration.STATE_TTL;

    private final ExpiringStore<OAuthState> store;
    private final SecureRandom random = new SecureRandom();

    OAuthStateStore(int capacity, Clock clock) {
        this.store = new ExpiringStore<>(capacity, clock);
    }

    /**
     * 发起一次往返，返回要交给平台的 state。
     *
     * <p>256 位随机数：state 的安全性来自不可猜测，而不是来自签名。
     *
     * @throws IllegalStateException 存储已满（正在被刷）。**不静默失败**——退回一个可猜的
     *                               state 等于把 CSRF 防线关掉
     */
    public String issue(OAuthProvider provider, OAuthPurpose purpose, Long userId) {
        return issue(provider, purpose, userId, false);
    }

    /**
     * 同 {@link #issue(OAuthProvider, OAuthPurpose, Long)}，但记下"这是桌面壳发起的"。
     *
     * <p>桌面壳的页面来源是 {@code http://127.0.0.1:5310}，回调落在远端服务器上，
     * 令牌必须经自定义协议送回应用，而不是留在浏览器里。
     */
    public String issue(OAuthProvider provider, OAuthPurpose purpose, Long userId, boolean desktop) {
        String state = randomToken();
        if (!store.put(state, new OAuthState(provider, purpose, userId, desktop), TTL)) {
            throw new IllegalStateException("第三方登录 state 存储已满，拒绝继续发起授权");
        }
        return state;
    }

    /**
     * 取出并作废。
     *
     * <p><b>必须用 take 而不是 get</b>：同一个 state 只允许一次回调。允许重放的话，
     * 拿到过这个 URL 的第三方（浏览器历史、Referer、日志）可以再走一遍。
     */
    public Optional<OAuthState> take(String state) {
        if (state == null || state.isBlank()) {
            return Optional.empty();
        }
        return store.take(state);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
