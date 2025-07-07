package com.xiaou.job.starter;

import com.xiaou.job.starter.XxlJobProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(XxlJobProperties.class)
public class XxlJobAutoConfiguration {

    private final XxlJobProperties properties;

    public XxlJobAutoConfiguration(XxlJobProperties properties) {
        this.properties = properties;
    }

    @Bean
    public XxlJobSpringExecutor xxlJobSpringExecutor() {
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(properties.getAdminAddresses());
        executor.setAppname(properties.getAppName());
        executor.setIp(properties.getIp());
        executor.setPort(properties.getPort());
        executor.setAccessToken(properties.getAccessToken());
        executor.setLogPath(properties.getLogPath());
        executor.setLogRetentionDays(properties.getLogRetentionDays());
        return executor;
    }
}
