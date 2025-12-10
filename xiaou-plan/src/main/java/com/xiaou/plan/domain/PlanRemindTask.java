package com.xiaou.plan.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 计划提醒任务实体类
 * 
 * @author xiaou
 */
@Data
public class PlanRemindTask {
    
    /**
     * 任务ID
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
     * 提醒类型：1-开始提醒 2-截止提醒
     */
    private Integer remindType;
    
    /**
     * 提醒日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate remindDate;
    
    /**
     * 提醒时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime remindTime;
    
    /**
     * 状态：0-待发送 1-已发送 2-已取消
     */
    private Integer status;
    
    /**
     * 实际发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime sendTime;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
