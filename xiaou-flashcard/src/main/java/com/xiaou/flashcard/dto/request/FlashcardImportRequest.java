package com.xiaou.flashcard.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 从面试题库导入闪卡请求
 *
 * @author xiaou
 */
@Data
public class FlashcardImportRequest {

    @NotNull(message = "卡组ID不能为空")
    private Long deckId;

    @NotEmpty(message = "题目ID列表不能为空")
    private List<Long> questionIds;
}
