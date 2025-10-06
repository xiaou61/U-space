package com.xiaou.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 文章发布响应DTO
 * 
 * @author xiaou
 */
@Data
public class ArticlePublishResponse {
    
    /**
     * 文章ID
     */
    private Long articleId;
    
    /**
     * 剩余积分
     */
    private Integer pointsRemaining;
    
    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;
}


