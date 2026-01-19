package com.xiaou.team.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 打卡记录实体类
 * 
 * @author xiaou
 */
@Data
public class StudyTeamCheckin {
    
    /**
     * 打卡ID
     */
    private Long id;
    
    /**
     * 小组ID
     */
    private Long teamId;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
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
     * 完成数量
     */
    private Integer completeValue;
    
    /**
     * 学习内容
     */
    private String content;
    
    /**
     * 图片URL，逗号分隔
     */
    private String images;
    
    /**
     * 心得感悟
     */
    private String feeling;
    
    /**
     * 关联题目ID
     */
    private String relatedQuestionIds;
    
    /**
     * 是否补卡
     */
    private Integer isSupplement;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 学习时长（分钟）
     */
    private Integer duration;
    
    /**
     * 相关链接
     */
    private String relatedLink;
    
    /**
     * 是否删除：0否 1是
     */
    private Integer isDeleted;
    
    /**
     * 创建人
     */
    private Long createBy;
    
    /**
     * 打卡时间
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
