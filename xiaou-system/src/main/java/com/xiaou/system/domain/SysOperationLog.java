package com.xiaou.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 *
 * @author xiaou
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysOperationLog {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 操作ID
     */
    private String operationId;

    /**
     * 操作模块
     */
    private String module;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求URI
     */
    private String requestUri;

    /**
     * HTTP请求方法
     */
    private String requestMethod;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应数据
     */
    private String responseData;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作IP
     */
    private String operatorIp;

    /**
     * 操作地点
     */
    private String operationLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 操作状态：0-成功，1-失败
     */
    private Integer status;

    /**
     * 错误消息
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime operationTime;

    /**
     * 耗时（毫秒）
     */
    private Long costTime;
} 