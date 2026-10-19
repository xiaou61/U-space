package io.github.xiaou61.web;

import org.springframework.boot.task.ThreadPoolTaskExecutorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 让异步任务继承 MDC（WORK-022 T05）。
 *
 * <p>不改 Spring Boot 自动配置的线程池本身，只挂一个 `TaskDecorator`：自动配置的
 * `applicationTaskExecutor` 会把所有 `ThreadPoolTaskExecutorCustomizer` 都应用上。
 * 这样连"线程池多大"都不用替项目做决定。
 */
@Configuration
public class TaskExecutionConfiguration {

    @Bean
    ThreadPoolTaskExecutorCustomizer mdcTaskDecoratorCustomizer() {
        MdcTaskDecorator decorator = new MdcTaskDecorator();
        return executor -> executor.setTaskDecorator(decorator);
    }
}
