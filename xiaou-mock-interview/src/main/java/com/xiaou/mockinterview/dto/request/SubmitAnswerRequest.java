package com.xiaou.mockinterview.dto.request;

import lombok.Data;

/**
 * 提交回答请求DTO
 *
 * @author xiaou
 */
@Data
public class SubmitAnswerRequest {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 问答记录ID
     */
    private Long qaId;

    /**
     * 用户回答内容
     */
    private String answer;

    /**
     * 回答用时（秒）
     */
    private Integer durationSeconds;
}
