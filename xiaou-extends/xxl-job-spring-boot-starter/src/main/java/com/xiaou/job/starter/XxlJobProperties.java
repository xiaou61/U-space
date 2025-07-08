package com.xiaou.job.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xxl.job")
@Data
public class XxlJobProperties {
    private boolean enabled = false; // 新增开关，默认关闭
    private String adminAddresses;
    private String appName;
    private String address;
    private String ip;
    private int port;
    private String accessToken;
    private String logPath = "/data/applogs/xxl-job/jobhandler";
    private int logRetentionDays = 30;
}
