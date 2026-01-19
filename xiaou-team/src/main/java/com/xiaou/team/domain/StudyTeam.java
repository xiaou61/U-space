package com.xiaou.team.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习小组实体类
 * 
 * @author xiaou
 */
@Data
public class StudyTeam {
    
    /**
     * 小组ID
     */
    private Long id;
    
    /**
     * 小组名称
     */
    private String teamName;
    
    /**
     * 小组简介
     */
    private String teamDesc;
    
    /**
     * 小组头像
     */
    private String teamAvatar;
    
    /**
     * 类型：1目标型 2学习型 3打卡型
     */
    private Integer teamType;
    
    /**
     * 标签，逗号分隔
     */
    private String tags;
    
    /**
     * 最大成员数
     */
    private Integer maxMembers;
    
    /**
     * 当前成员数
     */
    private Integer currentMembers;
    
    /**
     * 加入方式：1公开 2申请 3邀请
     */
    private Integer joinType;
    
    /**
     * 邀请码
     */
    private String inviteCode;
    
    /**
     * 目标标题
     */
    private String goalTitle;
    
    /**
     * 目标描述
     */
    private String goalDesc;
    
    /**
     * 目标开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate goalStartDate;
    
    /**
     * 目标结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate goalEndDate;
    
    /**
     * 每日目标量
     */
    private Integer dailyTarget;
    
    /**
     * 总打卡次数
     */
    private Integer totalCheckins;
    
    /**
     * 总讨论数
     */
    private Integer totalDiscussions;
    
    /**
     * 活跃天数
     */
    private Integer activeDays;
    
    /**
     * 创建者ID
     */
    private Long creatorId;
    
    /**
     * 状态：0已解散 1正常 2已满员
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
