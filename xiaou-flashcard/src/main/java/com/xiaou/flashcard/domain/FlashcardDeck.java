package com.xiaou.flashcard.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 闪卡卡组实体类
 *
 * @author xiaou
 */
@Data
public class FlashcardDeck {

    /**
     * 卡组ID
     */
    private Long id;

    /**
     * 创建者ID
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
     * 标签，逗号分隔
     */
    private String tags;

    /**
     * 是否公开: false-私有 true-公开
     */
    private Boolean isPublic;

    /**
     * 卡片数量
     */
    private Integer cardCount;

    /**
     * 学习人数
     */
    private Integer studyCount;

    /**
     * 复制次数
     */
    private Integer forkCount;

    /**
     * 复制来源ID
     */
    private Long sourceDeckId;

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

    /**
     * 删除标志: 0-正常 1-已删除
     */
    private Integer delFlag;

    // ========== 非数据库字段 ==========

    /**
     * 创建者用户名
     */
    private transient String creatorName;

    /**
     * 创建者头像
     */
    private transient String creatorAvatar;
}
