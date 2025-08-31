package com.xiaou.monitor.context;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 监控上下文信息
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class MonitorContext {

    /**
     * 跟踪ID
     */
    private String traceId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 用户名
     */
    private String username;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * HTTP方法
     */
    private String httpMethod;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作方法
     */
    private String method;

    /**
     * 请求开始时间
     */
    private Long startTime;
} 