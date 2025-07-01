package com.xiaou.sse.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 消息的dto
 */
@Data
public class SseMessageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 需要推送到的session key 列表
     */
    private List<String> userIds;

    /**
     * 需要发送的消息
     */
    private String message;
}
