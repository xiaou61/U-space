package com.xiaou.monitor.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * SQL监控日志实体
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class SqlMonitorLog {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 跟踪ID (用于关联同一次请求的多个SQL)
     */
    private String traceId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户类型 (admin/user)
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
     * MyBatis Mapper方法
     */
    private String mapperMethod;

    /**
     * SQL语句
     */
    private String sqlStatement;

    /**
     * SQL类型 (SELECT/INSERT/UPDATE/DELETE)
     */
    private String sqlType;

    /**
     * SQL参数
     */
    private String sqlParams;

    /**
     * 执行时间(毫秒)
     */
    private Long executionTime;

    /**
     * 影响行数
     */
    private Integer affectedRows;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 是否慢SQL
     */
    private Boolean slowSql;

    /**
     * 执行时间
     */
    private LocalDateTime executeTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
} 