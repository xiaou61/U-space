package com.xiaou.points.dto.lottery.admin;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量启用/禁用奖品请求
 * 
 * @author xiaou
 */
@Data
public class BatchToggleStatusRequest {
    
    /**
     * 奖品ID列表
     */
    @NotEmpty(message = "奖品ID列表不能为空")
    private List<Long> prizeIds;
    
    /**
     * 是否启用：true-启用，false-禁用
     */
    @NotNull(message = "状态不能为空")
    private Boolean isActive;
}

