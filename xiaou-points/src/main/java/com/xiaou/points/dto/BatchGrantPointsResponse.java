package com.xiaou.points.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量发放积分响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchGrantPointsResponse {
    
    /**
     * 成功发放的用户数量
     */
    private Integer successCount;
    
    /**
     * 失败的用户数量
     */
    private Integer failCount;
    
    /**
     * 总积分发放量
     */
    private Integer totalPointsGranted;
    
    /**
     * 发放详情
     */
    private List<GrantResult> grantResults;
    
    /**
     * 单个发放结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrantResult {
        private Long userId;
        private String userName;
        private Boolean success;
        private String message;
        private Integer currentBalance;
    }
}
