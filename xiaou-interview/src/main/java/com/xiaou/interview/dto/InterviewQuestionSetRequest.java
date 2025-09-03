package com.xiaou.interview.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 面试题单请求DTO
 *
 * @author xiaou
 */
@Data
public class InterviewQuestionSetRequest {

    /**
     * 题单标题
     */
    @NotBlank(message = "题单标题不能为空")
    @Size(max = 200, message = "题单标题长度不能超过200字符")
    private String title;

    /**
     * 题单描述
     */
    private String description;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 类型 (1-官方 2-用户创建)
     */
    private Integer type;

    /**
     * 可见性 (1-公开 2-私有) 仅用户创建题单有效
     */
    private Integer visibility;

    /**
     * 状态 (0-草稿 1-发布 2-下线)
     */
    private Integer status;
} 