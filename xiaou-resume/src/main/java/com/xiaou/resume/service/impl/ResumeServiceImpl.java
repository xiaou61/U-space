package com.xiaou.resume.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.filestorage.dto.FileUploadResult;
import com.xiaou.filestorage.service.FileStorageService;
import com.xiaou.resume.domain.*;
import com.xiaou.resume.dto.*;
import com.xiaou.resume.mapper.*;
import com.xiaou.resume.service.ResumeService;
import com.xiaou.resume.service.support.ResumeExportBuilder;
import com.xiaou.resume.service.support.ResumeExportBuilder.ExportedFile;
import com.xiaou.resume.support.ByteArrayMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 简历服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeTemplateMapper resumeTemplateMapper;
    private final ResumeInfoMapper resumeInfoMapper;
    private final ResumeSectionMapper resumeSectionMapper;
    private final ResumeVersionMapper resumeVersionMapper;
    private final ResumeShareMapper resumeShareMapper;
    private final ResumeAnalyticsMapper resumeAnalyticsMapper;
    private final FileStorageService fileStorageService;
    private final ResumeExportBuilder resumeExportBuilder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createResume(Long userId, ResumeCreateRequest request) {
        ResumeTemplate template = resumeTemplateMapper.selectById(request.getTemplateId());
        if (template == null) {
            throw new BusinessException("模板不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        ResumeInfo resume = new ResumeInfo()
                .setUserId(userId)
                .setResumeName(request.getResumeName())
                .setTemplateId(request.getTemplateId())
                .setSummary(request.getSummary())
                .setStatus(request.getStatus())
                .setVisibility(request.getVisibility())
                .setVersion(1)
                .setCreateTime(now)
                .setUpdateTime(now);
        resumeInfoMapper.insert(resume);

        List<ResumeSection> sections = buildSections(resume.getId(), request.getSections(), now);
        if (!sections.isEmpty()) {
            resumeSectionMapper.insertBatch(sections);
        }
        saveVersionSnapshot(resume, sections, "首次创建");
        ensureAnalyticsRecord(resume.getId());
        return resume.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResume(Long userId, Long resumeId, ResumeUpdateRequest request) {
        ResumeInfo dbResume = getOwnedResume(resumeId, userId);
        if (!dbResume.getTemplateId().equals(request.getTemplateId())) {
            if (resumeTemplateMapper.selectById(request.getTemplateId()) == null) {
                throw new BusinessException("模板不存在");
            }
        }
        LocalDateTime now = LocalDateTime.now();
        ResumeInfo update = new ResumeInfo()
                .setId(resumeId)
                .setResumeName(request.getResumeName())
                .setTemplateId(request.getTemplateId())
                .setSummary(request.getSummary())
                .setVisibility(request.getVisibility())
                .setStatus(request.getStatus())
                .setVersion(dbResume.getVersion() + 1)
                .setUpdateTime(now);
        resumeInfoMapper.updateById(update);

        resumeSectionMapper.deleteByResumeId(resumeId);
        List<ResumeSection> sections = buildSections(resumeId, request.getSections(), now);
        if (!sections.isEmpty()) {
            resumeSectionMapper.insertBatch(sections);
        }

        dbResume.setResumeName(request.getResumeName())
                .setTemplateId(request.getTemplateId())
                .setSummary(request.getSummary())
                .setVisibility(request.getVisibility())
                .setStatus(request.getStatus())
                .setVersion(update.getVersion())
                .setUpdateTime(now);
        saveVersionSnapshot(dbResume, sections, "内容更新");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResume(Long userId, Long resumeId) {
        getOwnedResume(resumeId, userId);
        resumeSectionMapper.deleteByResumeId(resumeId);
        resumeVersionMapper.deleteByResumeId(resumeId);
        resumeShareMapper.deleteByResumeId(resumeId);
        resumeAnalyticsMapper.deleteByResumeId(resumeId);
        resumeInfoMapper.deleteById(resumeId);
    }

    @Override
    public PageResult<ResumeInfo> listUserResumes(Long userId, ResumeListQueryRequest request) {
        ResumeListQueryRequest query = request == null ? new ResumeListQueryRequest() : request;
        return PageHelper.doPage(query.getPage(), query.getSize(),
                () -> resumeInfoMapper.selectByUserId(userId, query));
    }

    @Override
    public ResumePreviewResponse previewResume(Long userId, Long resumeId) {
        ResumePreviewResponse preview = buildPreview(resumeId, userId);
        ensureAnalyticsRecord(resumeId);
        resumeAnalyticsMapper.increaseViewCount(resumeId);
        resumeAnalyticsMapper.updateLastAccessTime(resumeId, LocalDateTime.now());
        return preview;
    }

    @Override
    public ResumeExportResponse exportResume(Long userId, Long resumeId, ResumeExportRequest request) {
        ResumePreviewResponse preview = buildPreview(resumeId, userId);
        String content = renderPlainText(preview);
        String format = StrUtil.blankToDefault(request.getFormat(), "PDF");
        ExportedFile exportedFile = resumeExportBuilder.buildFile(preview, request);
        MultipartFile multipartFile = new ByteArrayMultipartFile(
                exportedFile.fileName(),
                exportedFile.fileName(),
                exportedFile.contentType(),
                exportedFile.content());
        FileUploadResult uploadResult = fileStorageService.uploadSingle(
                multipartFile,
                "resume",
                "export-" + format.toLowerCase());
        if (uploadResult == null || !uploadResult.isSuccess()) {
            String message = uploadResult == null ? "文件上传失败" : uploadResult.getErrorMessage();
            throw new BusinessException("导出文件上传失败：" + message);
        }
        ResumeExportResponse response = new ResumeExportResponse();
        response.setFormat(format.toUpperCase())
                .setFileName(exportedFile.fileName())
                .setDownloadUrl(uploadResult.getAccessUrl())
                .setPreviewContent(content)
                .setVersionNumber(preview.getResume().getVersion())
                .setGeneratedAt(LocalDateTime.now());
        ensureAnalyticsRecord(resumeId);
        resumeAnalyticsMapper.increaseExportCount(resumeId);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResumeShareResponse createShareLink(Long userId, Long resumeId) {
        getOwnedResume(resumeId, userId);
        LocalDateTime now = LocalDateTime.now();
        ResumeShare existing = resumeShareMapper.selectActiveShare(resumeId, now);
        if (existing != null) {
            return toShareResponse(existing);
        }

        String code = RandomUtil.randomString(8).toUpperCase();
        ResumeShare share = new ResumeShare()
                .setResumeId(resumeId)
                .setShareCode(code)
                .setShareUrl("/resume/share/" + code)
                .setStatus(1)
                .setAccessCount(0)
                .setExpireTime(now.plusDays(7))
                .setCreateTime(now)
                .setUpdateTime(now);
        resumeShareMapper.insert(share);
        ensureAnalyticsRecord(resumeId);
        resumeAnalyticsMapper.increaseShareCount(resumeId);
        return toShareResponse(share);
    }

    @Override
    public ResumeAnalyticsResponse getResumeAnalytics(Long userId, Long resumeId) {
        getOwnedResume(resumeId, userId);
        ResumeAnalytics analytics = ensureAnalyticsRecord(resumeId);
        return toAnalyticsResponse(analytics);
    }

    @Override
    public ResumeAnalyticsSummary getPlatformAnalytics() {
        ResumeAnalyticsSummary summary = new ResumeAnalyticsSummary();
        summary.setTemplateCount(resumeTemplateMapper.countAll());
        summary.setResumeCount(resumeInfoMapper.countAll());
        summary.setPublishedResumeCount(resumeInfoMapper.countByStatus(1));
        summary.setDraftResumeCount(resumeInfoMapper.countByStatus(0));
        ResumeAnalytics total = resumeAnalyticsMapper.aggregateAll();
        if (total != null) {
            summary.setTotalViews(defaultValue(total.getViewCount()));
            summary.setTotalExports(defaultValue(total.getExportCount()));
            summary.setTotalShares(defaultValue(total.getShareCount()));
        } else {
            summary.setTotalViews(0L);
            summary.setTotalExports(0L);
            summary.setTotalShares(0L);
        }
        return summary;
    }

    @Override
    public List<ResumeHealthReport> getHealthReports() {
        List<ResumeHealthReport> reports = new ArrayList<>();
        List<ResumeInfo> noSectionResumes = resumeInfoMapper.selectResumesWithoutSections();
        LocalDateTime now = LocalDateTime.now();
        for (ResumeInfo resume : noSectionResumes) {
            ResumeHealthReport report = new ResumeHealthReport();
            report.setResumeId(resume.getId());
            report.setResumeName(resume.getResumeName());
            report.setIssue("简历缺少任何模块内容");
            report.setSeverity(2);
            report.setDetectedAt(now);
            reports.add(report);
        }

        List<ResumeShare> expiredShares = resumeShareMapper.selectExpiredShares(now);
        for (ResumeShare share : expiredShares) {
            ResumeHealthReport report = new ResumeHealthReport();
            report.setResumeId(share.getResumeId());
            report.setResumeName("RESUME-" + share.getResumeId());
            report.setIssue("存在过期分享链接待清理");
            report.setSeverity(1);
            report.setDetectedAt(now);
            reports.add(report);
        }
        return reports;
    }

    private ResumePreviewResponse buildPreview(Long resumeId, Long userId) {
        ResumeInfo resume = getOwnedResume(resumeId, userId);
        ResumeTemplate template = resumeTemplateMapper.selectById(resume.getTemplateId());
        List<ResumeSection> sections = resumeSectionMapper.selectByResumeId(resumeId);
        List<ResumeVersion> versions = resumeVersionMapper.selectByResumeId(resumeId);

        ResumePreviewResponse response = new ResumePreviewResponse();
        response.setResume(resume);
        response.setTemplate(template);
        response.setSections(convertToDto(sections));
        response.setVersions(versions);
        return response;
    }

    private List<ResumeSection> buildSections(Long resumeId, List<ResumeSectionDTO> items, LocalDateTime now) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        int sort = 1;
        List<ResumeSection> sections = new ArrayList<>();
        for (ResumeSectionDTO dto : items) {
            ResumeSection section = new ResumeSection()
                    .setResumeId(resumeId)
                    .setSectionType(dto.getSectionType())
                    .setTitle(dto.getTitle())
                    .setContent(dto.getContent())
                    .setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : sort++)
                    .setStatus(dto.getStatus() == null ? 1 : dto.getStatus())
                    .setCreateTime(now)
                    .setUpdateTime(now);
            sections.add(section);
        }
        return sections;
    }

    private void saveVersionSnapshot(ResumeInfo resume, List<ResumeSection> sections, String changeLog) {
        ResumeVersion version = new ResumeVersion()
                .setResumeId(resume.getId())
                .setVersionNumber(resume.getVersion())
                .setSnapshot(buildSnapshot(resume, sections))
                .setChangeLog(changeLog)
                .setCreatedBy(resume.getUserId())
                .setCreateTime(LocalDateTime.now());
        resumeVersionMapper.insert(version);
    }

    private String buildSnapshot(ResumeInfo resume, List<ResumeSection> sections) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("resume", resume);
        snapshot.put("sections", sections);
        return JSONUtil.toJsonStr(snapshot);
    }

    private ResumeInfo getOwnedResume(Long resumeId, Long userId) {
        ResumeInfo resume = resumeInfoMapper.selectByIdAndUserId(resumeId, userId);
        if (resume == null) {
            throw new BusinessException("无权访问该简历");
        }
        return resume;
    }

    private List<ResumeSectionDTO> convertToDto(List<ResumeSection> sections) {
        return sections.stream().map(section -> {
            ResumeSectionDTO dto = new ResumeSectionDTO();
            dto.setId(section.getId());
            dto.setSectionType(section.getSectionType());
            dto.setTitle(section.getTitle());
            dto.setContent(section.getContent());
            dto.setSortOrder(section.getSortOrder());
            dto.setStatus(section.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }

    private ResumeShareResponse toShareResponse(ResumeShare share) {
        ResumeShareResponse response = new ResumeShareResponse();
        response.setShareCode(share.getShareCode());
        response.setShareUrl(share.getShareUrl());
        response.setExpireTime(share.getExpireTime());
        return response;
    }

    private ResumeAnalyticsResponse toAnalyticsResponse(ResumeAnalytics analytics) {
        ResumeAnalyticsResponse response = new ResumeAnalyticsResponse();
        response.setViewCount(defaultValue(analytics.getViewCount()));
        response.setExportCount(defaultValue(analytics.getExportCount()));
        response.setShareCount(defaultValue(analytics.getShareCount()));
        response.setUniqueVisitors(defaultValue(analytics.getUniqueVisitors()));
        response.setLastAccessTime(analytics.getLastAccessTime());
        return response;
    }

    private long defaultValue(Long value) {
        return value == null ? 0L : value;
    }

    private ResumeAnalytics ensureAnalyticsRecord(Long resumeId) {
        ResumeAnalytics analytics = resumeAnalyticsMapper.selectByResumeId(resumeId);
        if (analytics == null) {
            LocalDateTime now = LocalDateTime.now();
            analytics = new ResumeAnalytics()
                    .setResumeId(resumeId)
                    .setViewCount(0L)
                    .setExportCount(0L)
                    .setShareCount(0L)
                    .setUniqueVisitors(0L)
                    .setCreateTime(now)
                    .setUpdateTime(now);
            resumeAnalyticsMapper.insert(analytics);
        }
        return analytics;
    }

    private String renderPlainText(ResumePreviewResponse preview) {
        StringBuilder builder = new StringBuilder();
        builder.append(preview.getResume().getResumeName()).append("\n")
                .append(preview.getResume().getSummary() == null ? "" : preview.getResume().getSummary())
                .append("\n\n");
        for (ResumeSectionDTO section : preview.getSections()) {
            builder.append("## ").append(section.getTitle()).append("\n");
            builder.append(section.getContent()).append("\n\n");
        }
        return builder.toString();
    }
}
