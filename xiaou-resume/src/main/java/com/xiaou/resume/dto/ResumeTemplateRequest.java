package com.xiaou.resume.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 模板创建/更新请求
 */
@Data
public class ResumeTemplateRequest {

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "模板分类不能为空")
    private String category;

    @Size(max = 500, message = "模板描述长度不能超过500个字符")
    private String description;

    private String coverUrl;
    private String previewUrl;
    private List<String> tags;
    private List<String> techStack;

    @NotNull(message = "经验层级不能为空")
    @Min(value = 1, message = "经验层级最小为1")
    @Max(value = 5, message = "经验层级最大为5")
    private Integer experienceLevel;

    @NotNull(message = "模板状态不能为空")
    private Integer status;
}
