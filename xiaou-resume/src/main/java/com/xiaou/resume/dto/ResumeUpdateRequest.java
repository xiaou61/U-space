package com.xiaou.resume.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新简历请求
 */
@Data
public class ResumeUpdateRequest {

    @NotBlank(message = "简历名称不能为空")
    @Size(max = 100, message = "简历名称不能超过100个字符")
    private String resumeName;

    @NotNull(message = "模板不能为空")
    private Long templateId;

    @Size(max = 500, message = "简介不能超过500个字符")
    private String summary;

    private Integer visibility;
    private Integer status;

    @Valid
    private List<ResumeSectionDTO> sections;
}
