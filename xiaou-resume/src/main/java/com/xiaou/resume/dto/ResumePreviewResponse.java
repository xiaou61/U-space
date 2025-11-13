package com.xiaou.resume.dto;

import com.xiaou.resume.domain.ResumeInfo;
import com.xiaou.resume.domain.ResumeTemplate;
import com.xiaou.resume.domain.ResumeVersion;
import lombok.Data;

import java.util.List;

/**
 * 简历预览响应
 */
@Data
public class ResumePreviewResponse {

    private ResumeInfo resume;
    private ResumeTemplate template;
    private List<ResumeSectionDTO> sections;
    private List<ResumeVersion> versions;
}
