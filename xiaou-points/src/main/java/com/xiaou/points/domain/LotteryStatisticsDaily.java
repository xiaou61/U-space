package com.xiaou.points.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 抽奖每日统计实体类
 * 
 * @author xiaou
 */
@Data
public class LotteryStatisticsDaily {
    
    /**
     * 统计ID
     */
    private Long id;
    
    /**
     * 统计日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate statDate;
    
    /**
     * 总抽奖次数
     */
    private Integer totalDrawCount;
    
    /**
     * 总消耗积分
     */
    private Long totalCostPoints;
    
    /**
     * 总奖励积分
     */
    private Long totalRewardPoints;
    
    /**
     * 实际回报率（%）
     */
    private BigDecimal actualReturnRate;
    
    /**
     * 平台利润积分
     */
    private Long profitPoints;
    
    /**
     * 平台利润积分（别名）
     */
    public Long getPlatformProfitPoints() {
        return profitPoints;
    }
    
    public void setPlatformProfitPoints(Long platformProfitPoints) {
        this.profitPoints = platformProfitPoints;
    }
    
    /**
     * 参与用户数
     */
    private Integer uniqueUserCount;
    
    /**
     * 人均抽奖次数
     */
    private BigDecimal avgDrawPerUser;
    
    /**
     * 特等奖中奖次数
     */
    private Integer specialPrizeCount;
    
    /**
     * 一等奖中奖次数
     */
    private Integer firstPrizeCount;
    
    /**
     * 二等奖中奖次数
     */
    private Integer secondPrizeCount;
    
    /**
     * 三等奖中奖次数
     */
    private Integer thirdPrizeCount;
    
    /**
     * 四等奖中奖次数
     */
    private Integer fourthPrizeCount;
    
    /**
     * 五等奖中奖次数
     */
    private Integer fifthPrizeCount;
    
    /**
     * 六等奖中奖次数
     */
    private Integer sixthPrizeCount;
    
    /**
     * 未中奖次数
     */
    private Integer noPrizeCount;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    /**
     * 计算总中奖次数（排除未中奖）
     */
    public Integer getTotalWinCount() {
        int total = 0;
        if (specialPrizeCount != null) total += specialPrizeCount;
        if (firstPrizeCount != null) total += firstPrizeCount;
        if (secondPrizeCount != null) total += secondPrizeCount;
        if (thirdPrizeCount != null) total += thirdPrizeCount;
        if (fourthPrizeCount != null) total += fourthPrizeCount;
        if (fifthPrizeCount != null) total += fifthPrizeCount;
        if (sixthPrizeCount != null) total += sixthPrizeCount;
        return total;
    }
}

