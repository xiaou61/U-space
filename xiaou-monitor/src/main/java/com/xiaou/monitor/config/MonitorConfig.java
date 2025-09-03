package com.xiaou.monitor.config;

import com.xiaou.monitor.interceptor.SqlMonitorInterceptor;
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
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

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
     * 是否启用异步保存监控日志
     */
    private boolean asyncSave = true;

    /**
     * 排除监控的mapper包路径
     */
    private List<String> excludeMapperPackages = Arrays.asList(
        "com.xiaou.monitor.mapper",
        "com.xiaou.system.mapper.SysLoginLogMapper",
        "com.xiaou.system.mapper.SysOperationLogMapper"
    );

    /**
     * 排除监控的请求路径
     */
    private List<String> excludeRequestPaths = Arrays.asList(
        "/monitor/",
        "/auth/login-logs",
        "/logs/"
    );

    /**
     * 排除监控的模块名称
     */
    private List<String> excludeModules = Arrays.asList(
        "SqlMonitor",
        "LogController", 
        "AuthController"
    );

    /**
     * 排除监控的方法名后缀
     */
    private List<String> excludeMethodSuffixes = Arrays.asList(
        "selectPage",
        "selectList", 
        "selectCount",
        "selectById",
        "selectByIds"
    );

    /**
     * 排除监控的mapper关键字
     */
    private List<String> excludeMapperKeywords = Arrays.asList(
        "Admin",
        "Log",
        "Monitor", 
        "Permission",
        "Role"
    );

    /**
     * 监控数据保留天数
     */
    private int retentionDays = 30;

    /**
     * 是否启用调试日志
     */
    private boolean debugEnabled = false;

    /**
     * 创建SQL监控拦截器Bean
     */
    @Bean
    public SqlMonitorInterceptor sqlMonitorInterceptor() {
        return new SqlMonitorInterceptor();
    }

    /**
     * 监听应用启动完成事件，注册SQL监控拦截器
     * 使用事件监听避免循环依赖问题
     */
    @EventListener(ApplicationReadyEvent.class)
    public void registerSqlMonitorInterceptor() {
        try {
            // 应用完全启动后再获取SqlSessionFactory，避免循环依赖
            SqlSessionFactory sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);
            SqlMonitorInterceptor interceptor = sqlMonitorInterceptor();
            
            sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
            log.info("SQL监控拦截器注册成功");
        } catch (Exception e) {
            log.warn("SQL监控拦截器注册失败: {}", e.getMessage(), e);
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