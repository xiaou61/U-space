package com.xiaou.activity.domain.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 用户积分余额响应
 */
@Data
public class UserPointsResp {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 积分类型ID
     */
    private Long pointsTypeId;

    /**
     * 积分类型名称
     */
    private String pointsTypeName;

    /**
     * 积分类型编码
     */
    private String pointsTypeCode;

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
} 