package com.xiaou.mockinterview.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面试会话响应DTO
 *
 * @author xiaou
 */
@Data
public class InterviewSessionResponse {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 面试方向代码
     */
    private String direction;

    /**
     * 面试方向名称
     */
    private String directionName;

    /**
     * 难度级别
     */
    private Integer level;

    /**
     * 难度级别名称
     */
    private String levelName;

    /**
     * 面试类型
     */
    private Integer interviewType;

    /**
     * AI风格
     */
    private Integer style;

    /**
     * AI风格名称
     */
    private String styleName;

    /**
     * 题目数量
     */
    private Integer questionCount;

    /**
     * 预计时长（分钟）
     */
    private Integer estimatedMinutes;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
