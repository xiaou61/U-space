package com.xiaou.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 博客开通响应DTO
 * 
 * @author xiaou
 */
@Data
public class BlogOpenResponse {
    
    /**
     * 博客ID
     */
    private Long blogId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 剩余积分
     */
    private Integer pointsRemaining;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}


