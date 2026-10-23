package io.github.xiaou61.account.internal.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 握手码的两条硬性质（WORK-037）：**只能换一次令牌**、**60 秒后失效**。
 *
 * <p>这个码能换到一组完整令牌，所以它的约束比普通 state 更硬。
 */
class HandoffStoreTest {

    private static final Instant NOW = Instant.parse("2026-10-23T06:00:00Z");

    @Test
    @DisplayName("握手码只能消费一次")
    void consumedOnce() {
        HandoffStore store = new HandoffStore(100, Clock.fixed(NOW, ZoneOffset.UTC));
        String handoff = store.issue(7L);

        assertThat(store.consume(handoff)).contains(7L);
        assertThat(store.consume(handoff))
                .as("握手码可重放，等于给了一个能在 60 秒窗口内反复换令牌的凭据")
                .isEmpty();
    }

    @Test
    @DisplayName("超过 60 秒后握手码失效")
    void expiresAfterTtl() {
        MutableClock clock = new MutableClock(NOW);
        HandoffStore store = new HandoffStore(100, clock);
        String handoff = store.issue(9L);

        clock.advance(OAuthConfiguration.HANDOFF_TTL.plusSeconds(1));

        assertThat(store.consume(handoff)).isEmpty();
    }

    @Test
    @DisplayName("空的握手码视为不存在")
    void blankHandoffIsRejected() {
        HandoffStore store = new HandoffStore(100, Clock.fixed(NOW, ZoneOffset.UTC));

        assertThat(store.consume(null)).isEmpty();
        assertThat(store.consume("")).isEmpty();
    }

    @Test
    @DisplayName("两个握手码互不影响，各换各的账号")
    void distinctHandoffsMapToDistinctUsers() {
        HandoffStore store = new HandoffStore(100, Clock.fixed(NOW, ZoneOffset.UTC));
        String first = store.issue(1L);
        String second = store.issue(2L);

        assertThat(first).isNotEqualTo(second);
        assertThat(store.consume(second)).contains(2L);
        assertThat(store.consume(first)).contains(1L);
    }

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
