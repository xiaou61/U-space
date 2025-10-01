package com.xiaou.points.dto;

import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量发放积分请求DTO
 * 
 * @author xiaou
 */
@Data
public class BatchGrantPointsRequest {
    
    /**
     * 用户ID列表
     */
    @NotNull(message = "用户ID列表不能为空")
    private List<Long> userIds;
    
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
