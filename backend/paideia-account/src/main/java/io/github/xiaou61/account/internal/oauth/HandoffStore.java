package io.github.xiaou61.account.internal.oauth;

import io.github.xiaou61.account.internal.support.ExpiringStore;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.Optional;

/**
 * 回调页与前端之间的一次性握手码（WORK-037）。
 *
 * <p><b>它解决的问题</b>：令牌在后端签发，但要交给前端。直接把 access/refresh 放进回调
 * 的跳转地址会留两个坑——refresh 有 14 天寿命却会落进浏览器历史与截图；桌面端还要跨进程传。
 *
 * <p><b>做法</b>：后端把签好的令牌与一个随机串关联存起来（60 秒），跳转地址里只放这个串。
 * 前端拿它换一次令牌。**URL 里永远不出现令牌本身**。
 *
 * <p><b>60 秒够不够</b>：够。这中间只隔一次页面跳转；给太久反而是把"可重放的链接"留在
 * 浏览器历史里。
 */
public final class HandoffStore {

    private static final java.time.Duration TTL = OAuthConfiguration.HANDOFF_TTL;

    private final ExpiringStore<Long> store;
    private final SecureRandom random = new SecureRandom();

    HandoffStore(int capacity, Clock clock) {
        this.store = new ExpiringStore<>(capacity, clock);
    }

    /**
     * 登记一次交接，返回放进 URL 的握手码。
     *
     * @param userId 已确认身份的账号
     */
    public String issue(long userId) {
        String handoff = randomToken();
        if (!store.put(handoff, userId, TTL)) {
            throw new IllegalStateException("第三方登录握手码存储已满");
        }
        return handoff;
    }

    /**
     * 取出并作废。
     *
     * <p>一次性是硬要求：这个码虽然只活 60 秒，但它能换到一组完整令牌，
     * 允许重放就等于给了一个可在窗口内反复使用的登录凭据。
     */
    public Optional<Long> consume(String handoff) {
        if (handoff == null || handoff.isBlank()) {
            return Optional.empty();
        }
        return store.take(handoff);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
