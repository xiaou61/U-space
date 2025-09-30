package com.xiaou.chat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 聊天室实体
 * 
 * @author xiaou
 */
@Data
public class ChatRoom {
    
    /**
     * 聊天室ID
     */
    private Long id;
    
    /**
     * 聊天室名称
     */
    private String roomName;
    
    /**
     * 类型：1官方群组
     */
    private Integer roomType;
    
    /**
     * 聊天室描述
     */
    private String description;
    
    /**
     * 最大人数限制，0表示不限制
     */
    private Integer maxUsers;
    
    /**
     * 状态：1正常 0禁用
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
