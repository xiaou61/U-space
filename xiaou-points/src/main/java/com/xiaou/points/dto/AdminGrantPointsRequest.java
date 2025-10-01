package com.xiaou.points.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 管理员发放积分请求DTO
 * 
 * @author xiaou
 */
@Data
public class AdminGrantPointsRequest {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 发放积分数量
     */
    @NotNull(message = "积分数量不能为空")
    @Min(value = 1, message = "积分数量最小为1")
    @Max(value = 10000, message = "积分数量最大为10000")
    private Integer points;
    
    /**
     * 发放原因
     */
    @NotBlank(message = "发放原因不能为空")
    private String reason;
}
