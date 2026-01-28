package com.xiaou.flashcard.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 学习热力图响应VO
 *
 * @author xiaou
 */
@Data
public class FlashcardHeatmapVO {

    private List<DailyData> data;

    @Data
    public static class DailyData {
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 学习卡片数
         */
        private Integer count;

        /**
         * 学习时长（分钟）
         */
        private Integer duration;

        /**
         * 热度等级：0-无 1-低 2-中 3-高 4-极高
         */
        private Integer level;
    }
}
