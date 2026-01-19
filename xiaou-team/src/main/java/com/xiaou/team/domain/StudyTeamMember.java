package com.xiaou.team.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 小组成员实体类
 * 
 * @author xiaou
 */
@Data
public class StudyTeamMember {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 小组ID
     */
    private Long teamId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 角色：1组长 2管理员 3成员
     */
    private Integer role;
    
    /**
     * 组内昵称
     */
    private String nickname;
    
    /**
     * 加入理由
     */
    private String joinReason;
    
    /**
     * 总打卡次数
     */
    private Integer totalCheckins;
    
    /**
     * 当前连续打卡
     */
    private Integer currentStreak;
    
    /**
     * 最长连续打卡
     */
    private Integer maxStreak;
    
    /**
     * 获得点赞数
     */
    private Integer totalLikesReceived;
    
    /**
     * 贡献积分
     */
    private Integer contributionPoints;
    
    /**
     * 贡献积分（别名，兼容service使用）
     */
    public Integer getContribution() {
        return contributionPoints;
    }
    
    public void setContribution(Integer contribution) {
        this.contributionPoints = contribution;
    }
    
    /**
     * 最后打卡时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastCheckinTime;
    
    /**
     * 状态：0已退出 1正常 2禁言中
     */
    private Integer status;
    
    /**
     * 禁言结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime muteEndTime;
    
    /**
     * 加入时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime joinTime;
    
    /**
     * 最后活跃时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastActiveTime;
}
