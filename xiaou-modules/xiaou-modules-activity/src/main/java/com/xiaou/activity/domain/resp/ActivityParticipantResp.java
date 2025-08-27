package com.xiaou.activity.domain.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 活动参与记录响应
 */
@Data
public class ActivityParticipantResp {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 参与时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date participateTime;

    /**
     * 参与状态(0:参与成功 1:已取消 2:违规取消)
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 参与排名(抢夺活动中的顺序)
     */
    private Integer participantRank;

    /**
     * 备注信息
     */
    private String remark;
} 