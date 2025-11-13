package com.xiaou.resume.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.resume.domain.ResumeInfo;
import com.xiaou.resume.dto.*;

import java.util.List;

/**
 * 简历服务
 */
public interface ResumeService {

    Long createResume(Long userId, ResumeCreateRequest request);

    void updateResume(Long userId, Long resumeId, ResumeUpdateRequest request);

    void deleteResume(Long userId, Long resumeId);

    PageResult<ResumeInfo> listUserResumes(Long userId, ResumeListQueryRequest request);

    ResumePreviewResponse previewResume(Long userId, Long resumeId);

    ResumeExportResponse exportResume(Long userId, Long resumeId, ResumeExportRequest request);

    ResumeShareResponse createShareLink(Long userId, Long resumeId);

    ResumeAnalyticsResponse getResumeAnalytics(Long userId, Long resumeId);

    ResumeAnalyticsSummary getPlatformAnalytics();

    List<ResumeHealthReport> getHealthReports();
}
