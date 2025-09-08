package com.xiaou.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;


import java.util.List;

/**
 * 批量发送消息请求DTO
 */
@Data
public class BatchSendRequest {
    
    /**
     * 接收者ID列表
     */
    @NotEmpty(message = "接收者列表不能为空")
    private List<Long> receiverIds;
    
    /**
     * 消息标题
     */
    @NotBlank(message = "消息标题不能为空")
    @Size(max = 200, message = "消息标题长度不能超过200字符")
    private String title;
    
    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;
    
    /**
     * 消息类型
     */
    private String type = "PERSONAL"; // 保持字符串以便前端传参，在Service层进行验证
} 