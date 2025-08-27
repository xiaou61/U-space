package com.xiaou.activity.domain.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 积分发放记录响应
 */
@Data
public class PointsRecordResp {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 积分类型ID
     */
    private Long pointsTypeId;

    /**
     * 积分类型名称
     */
    private String pointsTypeName;

    /**
     * 积分数量
     */
    private Integer pointsAmount;

    /**
     * 操作类型(1:获得 2:扣除)
     */
    private Integer operationType;

    /**
     * 操作类型名称
     */
    private String operationTypeName;

    /**
     * 发放状态(0:待发放 1:已发放 2:发放失败 3:已撤销)
     */
    private Integer status;

    /**
     * 发放状态名称
     */
    private String statusName;

    /**
     * 实际发放时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date grantTime;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 批量发放批次号
     */
    private String batchNo;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
} 