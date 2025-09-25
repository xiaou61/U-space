package com.xiaou.points.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户积分明细实体类
 * 
 * @author xiaou
 */
@Data
public class UserPointsDetail {
    
    /**
     * 明细ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 积分变动数量（正数为增加）
     */
    private Integer pointsChange;
    
    /**
     * 积分类型：1-后台发放 2-打卡积分
     */
    private Integer pointsType;
    
    /**
     * 变动描述/原因
     */
    private String description;
    
    /**
     * 变动后余额
     */
    private Integer balanceAfter;
    
    /**
     * 操作管理员ID（后台发放时记录）
     */
    private Long adminId;
    
    /**
     * 连续打卡天数（打卡积分时记录）
     */
    private Integer continuousDays;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
