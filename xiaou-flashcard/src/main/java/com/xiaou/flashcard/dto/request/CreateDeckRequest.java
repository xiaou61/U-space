package com.xiaou.flashcard.dto.request;

import lombok.Data;

/**
 * 创建卡组请求
 *
 * @author xiaou
 */
@Data
public class CreateDeckRequest {

    /**
     * 卡组名称
     */
    private String name;

    /**
     * 卡组描述
     */
    private String description;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 关联的面试题分类ID
     */
    private Long categoryId;

    /**
     * 分类标识（前端传入，如 java, spring 等）
     */
    private String category;

    /**
     * 是否公开: 0-私有 1-公开
     */
    private Integer isPublic;
}
