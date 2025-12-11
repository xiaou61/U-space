package com.xiaou.flashcard.dto.request;

import lombok.Data;

/**
 * 创建闪卡请求
 *
 * @author xiaou
 */
@Data
public class CreateCardRequest {

    /**
     * 所属卡组ID
     */
    private Long deckId;

    /**
     * 正面内容(问题)
     */
    private String frontContent;

    /**
     * 背面内容(答案)
     */
    private String backContent;
}
