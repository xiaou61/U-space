package com.xiaou.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志实体类
 *
 * @author xiaou
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SysLoginLog {

    /**
     * 日志ID
     */
    private Long id;

    /**
     * 管理员ID
     */
    private Long adminId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录IP
     */
    private String loginIp;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 登录状态：0-成功，1-失败
     */
    private Integer loginStatus;

    /**
     * 登录消息
     */
    private String loginMessage;

    /**
     * 登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginTime;
} 