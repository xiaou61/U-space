package com.xiaou.activity.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 用户积分余额表
 * @TableName u_user_points
 */
@TableName(value = "u_user_points")
@Data
public class UserPoints {
    
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
     * 积分类型ID
     */
    private Long pointsTypeId;

    /**
     * 当前积分余额
     */
    private Integer currentBalance;

    /**
     * 累计获得积分
     */
    private Integer totalEarned;

    /**
     * 累计消费积分
     */
    private Integer totalSpent;

    /**
     * 冻结积分（待发放的）
     */
    private Integer frozenPoints;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdateTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
} 