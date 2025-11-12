package com.xiaou.resume.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户侧统计响应
 */
@Data
public class ResumeAnalyticsResponse {

    private Long viewCount;
    private Long exportCount;
    private Long shareCount;
    private Long uniqueVisitors;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastAccessTime;
}
