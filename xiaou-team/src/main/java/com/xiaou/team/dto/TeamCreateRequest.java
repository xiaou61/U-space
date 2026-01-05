package com.xiaou.team.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建小组请求DTO
 * 
 * @author xiaou
 */
@Data
public class TeamCreateRequest {
    
    /**
     * 小组名称（2-50字符）
     */
    private String teamName;
    
    /**
     * 小组简介（最多500字）
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
     * 标签，逗号分隔（最多5个）
     */
    private String tags;
    
    /**
     * 最大成员数（2-50）
     */
    private Integer maxMembers;
    
    /**
     * 加入方式：1公开 2申请 3邀请
     */
    private Integer joinType;
    
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
}
