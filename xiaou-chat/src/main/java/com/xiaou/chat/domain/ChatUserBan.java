package com.xiaou.chat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 用户禁言实体
 * 
 * @author xiaou
 */
@Data
public class ChatUserBan {
    
    /**
     * 禁言记录ID
     */
    private Long id;
    
    /**
     * 被禁言用户ID
     */
    private Long userId;
    
    /**
     * 聊天室ID
     */
    private Long roomId;
    
    /**
     * 禁言原因
     */
    private String banReason;
    
    /**
     * 禁言开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date banStartTime;
    
    /**
     * 禁言结束时间，NULL表示永久
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date banEndTime;
    
    /**
     * 操作人ID
     */
    private Long operatorId;
    
    /**
     * 状态：1生效中 0已解除
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
