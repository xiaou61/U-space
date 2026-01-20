package com.xiaou.flashcard.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 批量创建闪卡请求
 *
 * @author xiaou
 */
@Data
public class FlashcardBatchCreateRequest {

    @NotNull(message = "卡组ID不能为空")
    private Long deckId;

    @NotEmpty(message = "闪卡列表不能为空")
    private List<FlashcardItem> cards;

    @Data
    public static class FlashcardItem {
        private String frontContent;
        private String backContent;
        private Integer contentType = 1;
        private String tags;
    }
}
