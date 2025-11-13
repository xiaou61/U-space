package com.xiaou.resume.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异常检测报告
 */
@Data
public class ResumeHealthReport {

    private Long resumeId;
    private String resumeName;
    private String issue;
    private Integer severity;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime detectedAt;
}
