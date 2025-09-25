package com.xiaou.points.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户积分排行响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointsRankingResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 用户昵称
     */
    private String nickName;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 总积分
     */
    private Integer totalPoints;
    
    /**
     * 积分对应人民币价值
     */
    private String balanceYuan;
    
    /**
     * 连续打卡天数
     */
    private Integer continuousDays;
    
    /**
     * 排名
     */
    private Integer ranking;
    
    /**
     * 注册时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
