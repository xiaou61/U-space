package com.xiaou.resume.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 简历模块DTO
 */
@Data
public class ResumeSectionDTO {

    private Long id;

    @NotBlank(message = "模块类型不能为空")
    private String sectionType;

    @NotBlank(message = "模块标题不能为空")
    private String title;

    @NotBlank(message = "模块内容不能为空")
    private String content;

    private Integer sortOrder;
    private Integer status;
}
