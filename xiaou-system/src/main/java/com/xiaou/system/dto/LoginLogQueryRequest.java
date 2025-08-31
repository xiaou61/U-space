package com.xiaou.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志查询请求DTO
 *
 * @author xiaou
 */
@Data
@Schema(description = "登录日志查询参数")
public class LoginLogQueryRequest {

    /**
     * 用户名
     */
    @Schema(description = "用户名，支持模糊查询", example = "admin")
    private String username;

    /**
     * 登录IP
     */
    @Schema(description = "登录IP地址", example = "192.168.1.100")
    private String loginIp;

    /**
     * 登录状态
     */
    @Schema(description = "登录状态：0-成功，1-失败", example = "0")
    private Integer loginStatus;

    /**
     * 登录地点
     */
    @Schema(description = "登录地点，支持模糊查询", example = "北京")
    private String loginLocation;

    /**
     * 开始时间
     */
    @Schema(description = "查询开始时间", example = "2024-01-01T00:00:00")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "查询结束时间", example = "2024-01-01T23:59:59")
    private LocalDateTime endTime;

    /**
     * 页码
     */
    @Schema(description = "页码，从1开始", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = 10;
} 