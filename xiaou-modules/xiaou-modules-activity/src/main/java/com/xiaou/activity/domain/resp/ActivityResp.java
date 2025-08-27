package com.xiaou.activity.domain.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 活动响应
 */
@Data
public class ActivityResp {

    /**
     * 活动ID
     */
    private Long id;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 活动描述
     */
    private String description;

    /**
     * 活动封面图片URL
     */
    private String coverImage;

    /**
     * 最大参与人数
     */
    private Integer maxParticipants;

    /**
     * 当前参与人数
     */
    private Integer currentParticipants;

    /**
     * 积分类型ID
     */
    private Long pointsTypeId;

    /**
     * 积分类型名称
     */
    private String pointsTypeName;

    /**
     * 奖励积分数量
     */
    private Integer pointsAmount;

    /**
     * 活动类型(1:抢夺型 2:签到型 3:任务型)
     */
    private Integer activityType;

    /**
     * 活动类型名称
     */
    private String activityTypeName;

    /**
     * 活动状态(0:待开始 1:进行中 2:已结束 3:已取消)
     */
    private Integer status;

    /**
     * 活动状态名称
     */
    private String statusName;

    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 活动结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 活动规则说明
     */
    private String rules;

    /**
     * 创建人用户ID
     */
    private String createUserId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 是否已参与
     */
    private Boolean participated;
} 