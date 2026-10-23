package io.github.xiaou61.account.internal.oauth;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 第三方登录的装配（WORK-037）。
 *
 * <p>两个一次性存储与既有的验证码、限流共用 {@code ExpiringStore}：同一套天花板
 * （进程内、单实例有效、重启即丢），因此没有引入新的限制类别。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OAuthProperties.class)
public class OAuthConfiguration {

    /** 授权页往返的合理上限：用户要在平台上登录、可能还要注册。再长就是在拖时间。 */
    static final java.time.Duration STATE_TTL = java.time.Duration.ofMinutes(5);

    /** 握手码只活 60 秒：它只是一次"回调页 → 前端"的转交，不该比一次页面跳转更久。 */
    static final java.time.Duration HANDOFF_TTL = java.time.Duration.ofSeconds(60);

    private static final int STATE_CAPACITY = 10_000;
    private static final int HANDOFF_CAPACITY = 10_000;

    @Bean
    OAuthStateStore oauthStateStore() {
        return new OAuthStateStore(STATE_CAPACITY, Clock.systemUTC());
    }

    @Bean
    HandoffStore oauthHandoffStore() {
        return new HandoffStore(HANDOFF_CAPACITY, Clock.systemUTC());
    }
}
