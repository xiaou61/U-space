package com.xiaou.flashcard.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交学习结果请求
 *
 * @author xiaou
 */
@Data
public class FlashcardStudySubmitRequest {

    @NotNull(message = "卡片ID不能为空")
    private Long cardId;

    /**
     * 评分：1-完全忘记 2-模糊记忆 3-想起来了 4-完全记住
     * 映射到SM-2的0-5评分
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最小为1")
    @Max(value = 4, message = "评分最大为4")
    private Integer quality;

    /**
     * 本次学习耗时（秒）
     */
    private Integer duration = 0;
}
