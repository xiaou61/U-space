package com.xiaou.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;



/**
 * 删除消息请求DTO
 */
@Data
public class DeleteMessageRequest {
    
    /**
     * 消息ID
     */
    @NotNull(message = "消息ID不能为空")
    private Long messageId;
} 