package com.xiaou.points.dto.lottery.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预警信息
 * 
 * @author xiaou
 */
@Data
@Builder
public class AlertInfo {
    
    /**
     * 预警时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime alertTime;
    
    /**
     * 预警级别：LOW-低 MEDIUM-中 HIGH-高 CRITICAL-严重
     */
    private String alertLevel;
    
    /**
     * 预警类型
     */
    private String alertType;
    
    /**
     * 预警消息
     */
    private String message;
    
    /**
     * 是否已处理
     */
    private Boolean handled;
    
    /**
     * 相关数据
     */
    private Object data;
}

