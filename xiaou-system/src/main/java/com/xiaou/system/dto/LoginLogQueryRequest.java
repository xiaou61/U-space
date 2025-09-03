package com.xiaou.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.common.core.domain.PageRequest;
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
public class LoginLogQueryRequest implements PageRequest {

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
    @Schema(description = "查询开始时间", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "查询结束时间", example = "2024-01-01 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
    
    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public LoginLogQueryRequest setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }
    
    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public LoginLogQueryRequest setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
} 