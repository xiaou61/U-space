package com.xiaou.points.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 抽奖记录实体类
 * 
 * @author xiaou
 */
@Data
public class LotteryDrawRecord {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 奖品ID
     */
    private Long prizeId;
    
    /**
     * 奖品等级
     */
    private Integer prizeLevel;
    
    /**
     * 获得积分
     */
    private Integer prizePoints;
    
    /**
     * 消耗积分
     */
    private Integer costPoints;
    
    /**
     * 实际中奖概率
     */
    private BigDecimal actualProbability;
    
    /**
     * 使用的抽奖策略
     */
    private String drawStrategy;
    
    /**
     * 抽奖IP
     */
    private String drawIp;
    
    /**
     * 抽奖设备
     */
    private String drawDevice;
    
    /**
     * 状态：1-成功 2-失败 3-已补偿
     */
    private Integer status;
    
    /**
     * 积分明细ID（扣减）
     */
    private Long costDetailId;
    
    /**
     * 积分明细ID（奖励）
     */
    private Long rewardDetailId;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 抽奖时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}

