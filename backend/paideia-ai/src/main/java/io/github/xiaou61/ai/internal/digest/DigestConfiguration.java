package io.github.xiaou61.ai.internal.digest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 速递的装配（WORK-027）。
 *
 * <p>两个定时任务只在 {@code paideia.ai.digest.enabled=true} 时注册：关闭时连 Bean 都不存在，
 * 而不是"存在但每次自己判断要不要跑"——后者会让调度器每天空转一次。
 */
@Configuration
@EnableConfigurationProperties(DigestProperties.class)
class DigestConfiguration {

    @Configuration
    @ConditionalOnProperty(prefix = "paideia.ai.digest", name = "enabled", havingValue = "true")
    static class SchedulingConfiguration {

        @Bean
        DigestJob digestJob(DigestCollectionService service) {
            return new DigestJob(service);
        }

        @Bean
        DigestWeeklyJob digestWeeklyJob(DigestWeeklyService service) {
            return new DigestWeeklyJob(service);
        }
    }
}
