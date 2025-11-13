package com.xiaou.resume.dto;

import lombok.Data;

/**
 * 平台统计汇总
 */
@Data
public class ResumeAnalyticsSummary {

    private Long templateCount;
    private Long resumeCount;
    private Long publishedResumeCount;
    private Long draftResumeCount;
    private Long totalViews;
    private Long totalExports;
    private Long totalShares;
}
