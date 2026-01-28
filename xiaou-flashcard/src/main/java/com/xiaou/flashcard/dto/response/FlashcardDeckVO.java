package com.xiaou.flashcard.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 卡组响应VO
 *
 * @author xiaou
 */
@Data
public class FlashcardDeckVO {

    private Long id;

    private Long userId;

    private String userName;

    private String userAvatar;

    private String name;

    private String description;

    private String coverImage;

    private String tags;

    private Boolean isPublic;

    private Integer cardCount;

    private Integer studyCount;

    private Integer forkCount;

    private Long sourceDeckId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 用户学习进度（已学习卡片数）
     */
    private Integer learnedCount;

    /**
     * 今日待复习数
     */
    private Integer dueCount;
}
