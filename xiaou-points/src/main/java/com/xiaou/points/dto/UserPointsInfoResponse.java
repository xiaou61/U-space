package com.xiaou.points.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户积分信息响应DTO
 * 用于单个用户积分信息查询
 *
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointsInfoResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 昵称
     */
    private String nickName;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 总积分
     */
    private Integer totalPoints;
    
    /**
     * 当前连续打卡天数
     */
    private Integer continuousDays;
    
    /**
     * 最后打卡日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate lastCheckinDate;
    
    /**
     * 本月总打卡天数
     */
    private Integer monthCheckinDays;
    
    /**
     * 积分账户创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 积分账户更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    /**
     * 是否今日已打卡
     */
    private Boolean hasCheckedToday;
}
