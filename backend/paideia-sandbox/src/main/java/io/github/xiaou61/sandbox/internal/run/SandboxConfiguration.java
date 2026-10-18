package io.github.xiaou61.sandbox.internal.run;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 沙箱配置装配（WORK-015）。
 *
 * <p>单独一个类：让 run 这个包自带宽入，新增与执行相关的 Bean 时不必回头动别的包。
 */
@Configuration
@EnableConfigurationProperties(SandboxProperties.class)
class SandboxConfiguration {
}
