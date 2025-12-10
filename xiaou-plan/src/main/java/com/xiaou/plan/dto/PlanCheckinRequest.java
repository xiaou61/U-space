package com.xiaou.plan.dto;

import lombok.Data;

/**
 * 计划打卡请求DTO
 * 
 * @author xiaou
 */
@Data
public class PlanCheckinRequest {
    
    /**
     * 计划ID
     */
    private Long planId;
    
    /**
     * 完成数量
     */
    private Integer completeValue;
    
    /**
     * 完成内容描述
     */
    private String completeContent;
    
    /**
     * 心得备注
     */
    private String remark;
}
