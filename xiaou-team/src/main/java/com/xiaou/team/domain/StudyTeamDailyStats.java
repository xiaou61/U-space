package com.xiaou.team.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 小组每日统计实体类
 * 
 * @author xiaou
 */
@Data
public class StudyTeamDailyStats {
    
    /**
     * 统计ID
     */
    private Long id;
    
    /**
     * 小组ID
     */
    private Long teamId;
    
    /**
     * 统计日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate statDate;
    
    /**
     * 打卡人数
     */
    private Integer checkinCount;
    
    /**
     * 当日成员数
     */
    private Integer memberCount;
    
    /**
     * 打卡率
     */
    private BigDecimal checkinRate;
    
    /**
     * 讨论数
     */
    private Integer discussionCount;
    
    /**
     * 新加入成员
     */
    private Integer newMemberCount;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
