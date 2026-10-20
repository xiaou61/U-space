package io.github.xiaou61.ai.internal.decision;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 决策模型的装配（WORK-026）。
 *
 * <p>单独一个类而不是挂到 {@code AiConfiguration} 上：让 decision 这个包自带宽入，
 * 将来换实现或加判定维度不必回头动别的包。
 */
@Configuration
@EnableConfigurationProperties(DecisionProperties.class)
class DecisionConfiguration {
}
