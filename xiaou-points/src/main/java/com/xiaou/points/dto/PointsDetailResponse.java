package com.xiaou.points.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分明细响应DTO
 * 
 * @author xiaou
 */
@Data
public class PointsDetailResponse {
    
    /**
     * 明细ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名（管理端使用）
     */
    private String userName;
    
    /**
     * 积分变动数量
     */
    private Integer pointsChange;
    
    /**
     * 积分类型代码
     */
    private Integer pointsType;
    
    /**
     * 积分类型描述
     */
    private String pointsTypeDesc;
    
    /**
     * 变动描述/原因
     */
    private String description;
    
    /**
     * 变动后余额
     */
    private Integer balanceAfter;
    
    /**
     * 操作管理员ID
     */
    private Long adminId;
    
    /**
     * 管理员名称（管理端使用）
     */
    private String adminName;
    
    /**
     * 连续打卡天数
     */
    private Integer continuousDays;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
