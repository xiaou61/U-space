package com.xiaou.flashcard.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 闪卡卡组实体
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class FlashcardDeck {

    /**
     * 卡组ID
     */
    private Long id;

    /**
     * 所属用户ID (0为系统卡组)
     */
    private Long userId;

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
     * 分类标识（如 java, spring 等）
     */
    private String category;

    /**
     * 闪卡总数
     */
    private Integer cardCount;

    /**
     * 是否公开: 0-私有 1-公开
     */
    private Integer isPublic;

    /**
     * 是否官方: 0-用户 1-官方
     */
    private Integer isOfficial;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 状态: 0-禁用 1-正常
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    // ====== 扩展字段（非数据库字段）======

    /**
     * 用户已掌握的卡片数量
     */
    private Integer masteredCount;

    /**
     * 用户学习中的卡片数量
     */
    private Integer learningCount;

    /**
     * 用户待复习的卡片数量
     */
    private Integer reviewCount;

    /**
     * 新卡片数量（未学习过的卡片）
     */
    private Integer newCount;
}
