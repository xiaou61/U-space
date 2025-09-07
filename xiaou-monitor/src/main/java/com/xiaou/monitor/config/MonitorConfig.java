package com.xiaou.monitor.config;

import com.xiaou.monitor.interceptor.SqlInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;



/**
 * 监控模块配置类
 *
 * @author xiaou
 */
@Slf4j
@Configuration
@EnableAsync
@EnableScheduling
@RequiredArgsConstructor
@Data
@ConfigurationProperties(prefix = "xiaou.monitor")
public class MonitorConfig {

    private final ApplicationContext applicationContext;

    /**
     * 是否启用SQL监控
     */
    private boolean enabled = true;

    /**
     * 慢SQL阈值(毫秒)
     */
    private long slowSqlThreshold = 1000L;

    /**
     * 是否启用调试日志
     */
    private boolean debugEnabled = false;

    /**
     * SQL调用树追踪开关
     */
    private boolean treeTraceEnabled = true;

    /**
     * 最大会话数量
     */
    private int maxSessionCount = 100;

    /**
     * 会话过期时间（分钟）
     */
    private int sessionExpireMinutes = 60;

    /**
     * 创建SQL调用树拦截器Bean
     */
    @Bean
    public SqlInterceptor sqlInterceptor() {
        return new SqlInterceptor();
    }

    /**
     * 监听应用启动完成事件，注册SQL调用树拦截器
     * 使用事件监听避免循环依赖问题
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerSqlInterceptor() {
        try {
            // 应用完全启动后再获取SqlSessionFactory，避免循环依赖
            SqlSessionFactory sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);
            SqlInterceptor interceptor = sqlInterceptor();
            
            sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
            log.info("SQL调用树拦截器注册成功");
        } catch (Exception e) {
            log.warn("SQL调用树拦截器注册失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 监控任务异步执行器
     */
    @Bean("monitorTaskExecutor")
    public Executor monitorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("monitor-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
} 