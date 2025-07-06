package com.xiaou.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 用户对话记录表
 * @TableName u_chat_message
 */
@TableName(value ="u_chat_message")
@Data
public class ChatMessage {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户提问内容
     */
    private String userContent;

    /**
     * AI回复内容
     */
    private String aiContent;

    /**
     * 是否启用RAG
     */
    private Boolean enableRag;

    /**
     * 是否启用工具
     */
    private Boolean enableTools;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}