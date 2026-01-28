package com.xiaou.flashcard.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建卡组请求
 *
 * @author xiaou
 */
@Data
public class FlashcardDeckCreateRequest {

    @NotBlank(message = "卡组名称不能为空")
    @Size(max = 100, message = "卡组名称不能超过100个字符")
    private String name;

    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;

    private String coverImage;

    private String tags;

    private Boolean isPublic = false;
}
