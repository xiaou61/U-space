package com.xiaou.points.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员发放积分响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGrantPointsResponse {
    
    /**
     * 明细记录ID
     */
    private Long detailId;
    
    /**
     * 用户当前积分余额
     */
    private Integer userBalance;
    
    /**
     * 积分对应人民币价值
     */
    private String balanceYuan;
    
    /**
     * 用户名
     */
    private String userName;
}
