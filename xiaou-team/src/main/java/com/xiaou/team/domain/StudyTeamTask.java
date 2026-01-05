package com.xiaou.team.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡任务实体类
 * 
 * @author xiaou
 */
@Data
public class StudyTeamTask {
    
    /**
     * 任务ID
     */
    private Long id;
    
    /**
     * 小组ID
     */
    private Long teamId;
    
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
     * 自定义重复日
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
    /**
     * 创建人 ID
     */
    private Long creatorId;
    
    /**
     * 状态：0已结束 1进行中
     */
    private Integer status;
    
    /**
     * 是否删除：0否 1是
     */
    private Integer isDeleted;
    
    /**
     * 创建人
     */
    private Long createBy;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新人
     */
    private Long updateBy;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}

