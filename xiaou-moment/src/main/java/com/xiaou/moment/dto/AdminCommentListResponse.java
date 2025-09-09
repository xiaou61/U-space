package com.xiaou.moment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端评论列表响应
 */
@Data
public class AdminCommentListResponse {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 动态ID
     */
    private Long momentId;
    
    /**
     * 动态内容摘要
     */
    private String momentContentSummary;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
} 