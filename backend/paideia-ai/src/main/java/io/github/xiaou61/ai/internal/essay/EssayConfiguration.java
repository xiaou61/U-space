package io.github.xiaou61.ai.internal.essay;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 八股文专区的装配（WORK-025）。
 *
 * <p>定时任务只在 {@code paideia.ai.essay.enabled=true} 时注册：关闭时连 Bean 都不存在，
 * 而不是"存在但每次自己判断要不要跑"——后者会让调度器每天空转一次。
 */
@Configuration
@EnableConfigurationProperties(EssayProperties.class)
class EssayConfiguration {

    /** 抓取任务。单独一个配置类只为让 {@code @ConditionalOnProperty} 只作用于它。 */
    @Configuration
    @ConditionalOnProperty(prefix = "paideia.ai.essay", name = "enabled", havingValue = "true")
    static class SchedulingConfiguration {

        @org.springframework.context.annotation.Bean
        EssayCollectionJob essayCollectionJob(EssayCollectionService service) {
            return new EssayCollectionJob(service);
        }
    }
}
