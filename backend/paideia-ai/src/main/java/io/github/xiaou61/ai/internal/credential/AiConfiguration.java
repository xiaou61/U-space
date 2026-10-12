package io.github.xiaou61.ai.internal.credential;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 模块的装配。
 *
 * <p>这里只做一件事：把 {@link AiProperties} 注册成配置对象。没有别的 Bean——
 * 加了 SSRF 校验的传输层（{@code GuardedHttpTransport}）自己是 {@code @Component}，
 * 在这里再声明一次只会得到两个同类型的 Bean，让注入变成歧义。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AiProperties.class)
public class AiConfiguration {
}
