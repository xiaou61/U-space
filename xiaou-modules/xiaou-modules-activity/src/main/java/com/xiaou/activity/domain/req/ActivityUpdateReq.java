package com.xiaou.activity.domain.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


import java.util.Date;

/**
 * 更新活动请求
 */
@Data
public class ActivityUpdateReq {

    /**
     * 活动ID
     */
    @NotNull(message = "活动ID不能为空")
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
    @Min(value = 1, message = "最大参与人数必须大于0")
    private Integer maxParticipants;

    /**
     * 积分类型ID
     */
    private Long pointsTypeId;

    /**
     * 奖励积分数量
     */
    @Min(value = 0, message = "奖励积分数量不能为负数")
    private Integer pointsAmount;

    /**
     * 活动类型(1:抢夺型 2:签到型 3:任务型)
     */
    private Integer activityType;

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
} 