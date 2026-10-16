package io.github.xiaou61.ai.internal.billing;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 计费配置的装配。
 *
 * <p>单独一个类而不是挂到 {@code AiConfiguration} 上：让 billing 这个包自带宽入，新增计费相关的
 * Bean（积分服务、审计查询）时不必回头动别的包。
 */
@Configuration
@EnableConfigurationProperties(BillingProperties.class)
class BillingConfiguration {
}
