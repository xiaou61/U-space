package com.xiaou.flashcard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建闪卡请求
 *
 * @author xiaou
 */
@Data
public class FlashcardCreateRequest {

    @NotNull(message = "卡组ID不能为空")
    private Long deckId;

    @NotBlank(message = "正面内容不能为空")
    private String frontContent;

    @NotBlank(message = "背面内容不能为空")
    private String backContent;

    /**
     * 内容类型：1-文本 2-Markdown 3-代码
     */
    private Integer contentType = 1;

    private String tags;

    private Integer sortOrder;
}
