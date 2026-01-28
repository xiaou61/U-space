package com.xiaou.flashcard.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新卡组请求
 *
 * @author xiaou
 */
@Data
public class FlashcardDeckUpdateRequest {

    @NotNull(message = "卡组ID不能为空")
    private Long id;

    @Size(max = 100, message = "卡组名称不能超过100个字符")
    private String name;

    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;

    private String coverImage;

    private String tags;

    private Boolean isPublic;
}
