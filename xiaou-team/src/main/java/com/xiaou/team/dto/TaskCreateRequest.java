package com.xiaou.team.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建打卡任务请求DTO
 * 
 * @author xiaou
 */
@Data
public class TaskCreateRequest {
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 任务描述
     */
    private String taskDesc;
    
    /**
     * 类型：1刷题 2学习时长 3阅读 4自定义
     */
    private Integer taskType;
    
    /**
     * 目标数量
     */
    private Integer targetValue;
    
    /**
     * 目标单位
     */
    private String targetUnit;
    
    /**
     * 重复：1每日 2工作日 3自定义
     */
    private Integer repeatType;
    
    /**
     * 自定义重复日（如：1,2,3,4,5）
     */
    private String repeatDays;
    
    /**
     * 是否必须附带内容
     */
    private Integer requireContent;
    
    /**
     * 是否必须附带图片
     */
    private Integer requireImage;
    
    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startDate;
    
    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endDate;
}
