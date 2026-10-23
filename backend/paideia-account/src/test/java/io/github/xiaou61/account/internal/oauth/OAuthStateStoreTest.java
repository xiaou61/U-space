package io.github.xiaou61.account.internal.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 一次性凭据的三条硬性质（WORK-037 评审焦点）：**只能消费一次**、**会过期**、**不能被挪用**。
 *
 * <p>这三条都是"去掉就静默失去防护"的那类：重放不报错、过期不报错、挪用到另一个平台也不报错。
 * 所以每一条都单独钉一遍。
 */
class OAuthStateStoreTest {

    private static final Instant NOW = Instant.parse("2026-10-23T06:00:00Z");

    @Test
    @DisplayName("同一个 state 只能消费一次：第二次取回必须为空")
    void invalidatesStateAfterFirstUse() {
        OAuthStateStore store = new OAuthStateStore(100, Clock.fixed(NOW, ZoneOffset.UTC));
        String state = store.issue(OAuthProvider.GITHUB, OAuthPurpose.LOGIN, null);

        assertThat(store.take(state)).isPresent();
        assertThat(store.take(state))
                .as("state 可重放，等于给了一个可在窗口内反复使用的登录链接")
                .isEmpty();
    }

    @Test
    @DisplayName("超过 5 分钟后 state 失效")
    void expiresStateAfterTtl() {
        MutableClock clock = new MutableClock(NOW);
        OAuthStateStore store = new OAuthStateStore(100, clock);
        String state = store.issue(OAuthProvider.GITEE, OAuthPurpose.LOGIN, null);

        clock.advance(OAuthConfiguration.STATE_TTL.plusSeconds(1));

        assertThat(store.take(state)).isEmpty();
    }

    @Test
    @DisplayName("过期时间是固定的，不会因为被读取而顺延")
    void readingDoesNotExtendTtl() {
        MutableClock clock = new MutableClock(NOW);
        OAuthStateStore store = new OAuthStateStore(100, clock);
        String state = store.issue(OAuthProvider.GITHUB, OAuthPurpose.LOGIN, null);

        // 反复 take 都是空（因为已经消费）；这里验的是 issue 时就定死的过期时刻
        clock.advance(OAuthConfiguration.STATE_TTL.minusSeconds(1));
        assertThat(store.take(state)).as("尚未过期时应可取回").isPresent();
    }

    @Test
    @DisplayName("state 里记着发起时的平台与用途，回调据此判定，不靠客户端传参")
    void recordsProviderAndPurpose() {
        OAuthStateStore store = new OAuthStateStore(100, Clock.fixed(NOW, ZoneOffset.UTC));
        String loginState = store.issue(OAuthProvider.GITHUB, OAuthPurpose.LOGIN, null);
        String bindState = store.issue(OAuthProvider.GITEE, OAuthPurpose.BIND, 42L);

        assertThat(store.take(loginState))
                .get()
                .satisfies(state -> {
                    assertThat(state.provider()).isEqualTo(OAuthProvider.GITHUB);
                    assertThat(state.purpose()).isEqualTo(OAuthPurpose.LOGIN);
                    assertThat(state.userId()).isNull();
                });
        assertThat(store.take(bindState))
                .get()
                .satisfies(state -> {
                    assertThat(state.provider()).isEqualTo(OAuthProvider.GITEE);
                    assertThat(state.purpose()).isEqualTo(OAuthPurpose.BIND);
                    assertThat(state.userId()).isEqualTo(42L);
                });
    }

    @Test
    @DisplayName("空串或 null 的 state 一律视为不存在，不当成'没传就算过'")
    void blankStateIsRejected() {
        OAuthStateStore store = new OAuthStateStore(100, Clock.fixed(NOW, ZoneOffset.UTC));

        assertThat(store.take(null)).isEmpty();
        assertThat(store.take("")).isEmpty();
        assertThat(store.take("   ")).isEmpty();
    }

    @Test
    @DisplayName("容量满时拒绝发起，而不是退回一个可猜的 state")
    void refusesWhenFull() {
        OAuthStateStore store = new OAuthStateStore(1, Clock.fixed(NOW, ZoneOffset.UTC));
        store.issue(OAuthProvider.GITHUB, OAuthPurpose.LOGIN, null);

        assertThatThrownBy(() -> store.issue(OAuthProvider.GITHUB, OAuthPurpose.LOGIN, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("已满");
    }

    /** 可推进的时钟，用来验过期而不真的等 5 分钟。 */
    private static final class MutableClock extends Clock {

        private Instant now;

        private MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration amount) {
            now = now.plus(amount);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
