package com.xiaou.plan.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 计划打卡记录实体类
 * 
 * @author xiaou
 */
@Data
public class PlanCheckinRecord {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 计划ID
     */
    private Long planId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 打卡日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate checkinDate;
    
    /**
     * 打卡时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkinTime;
    
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
    
    /**
     * 是否补卡：0-否 1-是
     */
    private Integer isSupplement;
    
    /**
     * 获得积分
     */
    private Integer pointsEarned;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
