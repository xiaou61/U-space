package com.xiaou.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志查询请求DTO
 *
 * @author xiaou
 */
@Data
@Schema(description = "操作日志查询请求")
public class OperationLogQueryRequest {

    /**
     * 操作模块
     */
    @Schema(description = "操作模块", example = "用户管理")
    private String module;

    /**
     * 操作类型
     */
    @Schema(description = "操作类型", example = "UPDATE")
    private String operationType;

    /**
     * 操作人姓名
     */
    @Schema(description = "操作人姓名", example = "admin")
    private String operatorName;

    /**
     * 操作状态：0-成功，1-失败
     */
    @Schema(description = "操作状态：0-成功，1-失败", example = "0")
    private Integer status;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间", example = "2024-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间", example = "2024-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 页码
     */
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;
} 