package com.xiaou.points.dto.lottery;

import lombok.Data;

/**
 * 风险用户查询请求
 * 
 * @author xiaou
 */
@Data
public class RiskUserQueryRequest {
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer size = 20;
    
    /**
     * 风险等级：0-正常 1-低风险 2-中风险 3-高风险
     */
    private Integer riskLevel;
    
    /**
     * 是否黑名单：0-否 1-是
     */
    private Integer isBlacklist;
    
    /**
     * 最小连续未中奖次数
     */
    private Integer minContinuousNoWin;
}

