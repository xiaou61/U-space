package com.xiaou.activity.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 积分发放记录表
 * @TableName u_points_record
 */
@TableName(value = "u_points_record")
@Data
public class PointsRecord {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 积分类型ID
     */
    private Long pointsTypeId;

    /**
     * 积分数量
     */
    private Integer pointsAmount;

    /**
     * 操作类型(1:获得 2:扣除)
     */
    private Integer operationType;

    /**
     * 发放状态(0:待发放 1:已发放 2:发放失败 3:已撤销)
     */
    private Integer status;

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

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
} 