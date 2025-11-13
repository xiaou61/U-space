package com.xiaou.codepen.dto;

import lombok.Data;

/**
 * 推荐请求
 * 
 * @author xiaou
 */
@Data
public class RecommendRequest {
    
    /**
     * 作品ID
     */
    private Long id;
    
    /**
     * 推荐过期时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String expireTime;
}

