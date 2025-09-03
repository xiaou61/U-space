package com.xiaou.interview.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Markdown导入请求DTO
 *
 * @author xiaou
 */
@Data
public class MarkdownImportRequest {

    /**
     * 题单ID
     */
    @NotNull(message = "题单ID不能为空")
    private Long questionSetId;

    /**
     * Markdown内容
     */
    @NotBlank(message = "Markdown内容不能为空")
    private String markdownContent;

    /**
     * 是否覆盖已有题目 (true-覆盖 false-追加)
     */
    private Boolean overwrite = false;
} 