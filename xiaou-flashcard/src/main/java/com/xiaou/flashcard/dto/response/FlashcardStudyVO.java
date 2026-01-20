package com.xiaou.flashcard.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习卡片响应VO
 *
 * @author xiaou
 */
@Data
public class FlashcardStudyVO {

    private Long id;

    private Long deckId;

    private String deckName;

    private String frontContent;

    private String backContent;

    private Integer contentType;

    private String tags;

    /**
     * 是否是新卡片（未学习过）
     */
    private Boolean isNew;

    /**
     * 掌握程度：1-新学 2-学习中 3-已掌握
     */
    private Integer masteryLevel;

    /**
     * 上次复习时间
     */
    private LocalDateTime lastReviewTime;

    /**
     * 总复习次数
     */
    private Integer totalReviews;
}
