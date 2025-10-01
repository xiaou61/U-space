package com.xiaou.points.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户打卡位图实体类
 * 
 * @author xiaou
 */
@Data
public class UserCheckinBitmap {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 年月，格式：YYYY-MM
     */
    private String yearMonth;
    
    /**
     * 打卡位图，每位代表当月某天
     */
    private Long checkinBitmap;
    
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
     * 当月总打卡天数
     */
    private Integer totalCheckinDays;
    
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
}
