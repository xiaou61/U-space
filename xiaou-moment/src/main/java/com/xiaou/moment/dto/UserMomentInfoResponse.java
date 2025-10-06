package com.xiaou.moment.dto;

import lombok.Data;

/**
 * 用户动态信息响应DTO
 */
@Data
public class UserMomentInfoResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 动态总数
     */
    private Long totalMoments;
    
    /**
     * 总获赞数
     */
    private Long totalLikes;
    
    /**
     * 总评论数
     */
    private Long totalComments;
}

