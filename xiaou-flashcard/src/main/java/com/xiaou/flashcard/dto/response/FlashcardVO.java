package com.xiaou.flashcard.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 闪卡响应VO
 *
 * @author xiaou
 */
@Data
public class FlashcardVO {

    private Long id;

    private Long deckId;

    private String frontContent;

    private String backContent;

    private Integer contentType;

    private String tags;

    private Long sourceQuestionId;

    private Integer sortOrder;

    private LocalDateTime createTime;

    /**
     * 用户学习状态（仅登录用户有值）
     */
    private StudyStatusVO studyStatus;

    @Data
    public static class StudyStatusVO {
        /**
         * 掌握程度：1-新学 2-学习中 3-已掌握
         */
        private Integer masteryLevel;

        /**
         * 下次复习时间
         */
        private LocalDateTime nextReviewTime;

        /**
         * 总复习次数
         */
        private Integer totalReviews;

        /**
         * 正确次数
         */
        private Integer correctCount;
    }
}
