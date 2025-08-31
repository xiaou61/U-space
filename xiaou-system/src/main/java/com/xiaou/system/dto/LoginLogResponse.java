package com.xiaou.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志响应DTO
 *
 * @author xiaou
 */
@Data
@Schema(description = "登录日志信息")
public class LoginLogResponse {

    /**
     * 日志ID
     */
    @Schema(description = "日志ID", example = "1")
    private Long id;

    /**
     * 管理员ID
     */
    @Schema(description = "管理员ID", example = "1")
    private Long adminId;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "admin")
    private String username;

    /**
     * 登录IP
     */
    @Schema(description = "登录IP地址", example = "192.168.1.100")
    private String loginIp;

    /**
     * 登录地点
     */
    @Schema(description = "登录地点", example = "北京市")
    private String loginLocation;

    /**
     * 浏览器
     */
    @Schema(description = "浏览器信息", example = "Chrome 120.0.0.0")
    private String browser;

    /**
     * 操作系统
     */
    @Schema(description = "操作系统", example = "Windows 10")
    private String os;

    /**
     * 登录状态
     */
    @Schema(description = "登录状态：0-成功，1-失败", example = "0")
    private Integer loginStatus;

    /**
     * 登录状态描述
     */
    @Schema(description = "登录状态描述", example = "登录成功")
    private String loginStatusText;

    /**
     * 登录消息
     */
    @Schema(description = "登录消息", example = "登录成功")
    private String loginMessage;

    /**
     * 登录时间
     */
    @Schema(description = "登录时间", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginTime;

    /**
     * 设置登录状态描述
     */
    public void setLoginStatus(Integer loginStatus) {
        this.loginStatus = loginStatus;
        this.loginStatusText = loginStatus != null && loginStatus == 0 ? "成功" : "失败";
    }
} 