package com.xiaou.monitor.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * SQL监控查询请求
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class SqlMonitorQueryRequest {

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;

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
     * SQL类型
     */
    private String sqlType;

    /**
     * 是否只查询慢SQL
     */
    private Boolean slowSqlOnly;

    /**
     * 是否只查询失败SQL
     */
    private Boolean errorOnly;

    /**
     * 最小执行时间(毫秒)
     */
    private Long minExecutionTime;

    /**
     * 最大执行时间(毫秒)
     */
    private Long maxExecutionTime;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * SQL语句关键字
     */
    private String sqlKeyword;

    /**
     * Mapper方法关键字
     */
    private String mapperKeyword;

    /**
     * 排序字段
     */
    private String orderBy = "create_time";

    /**
     * 排序方向
     */
    private String orderDirection = "DESC";
} 