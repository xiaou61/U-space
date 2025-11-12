package com.xiaou.resume.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 导出结果
 */
@Data
@Accessors(chain = true)
public class ResumeExportResponse {

    private String format;
    private String fileName;
    private String previewContent;
    private String downloadUrl;
    private Integer versionNumber;
    private LocalDateTime generatedAt;
}
