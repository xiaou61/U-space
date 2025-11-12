package com.xiaou.resume.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 导出请求
 */
@Data
public class ResumeExportRequest {

    @NotBlank(message = "导出格式不能为空")
    private String format;

    private String theme;
    private Boolean watermark = Boolean.FALSE;
}
