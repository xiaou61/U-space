package com.xiaou.community.dto;

import com.xiaou.community.domain.CommunityTag;
import lombok.Data;

import java.util.List;

/**
 * 用户主页信息响应
 * 
 * @author xiaou
 */
@Data
public class CommunityUserProfileResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * 统计数据
     */
    private UserStats stats;
    
    /**
     * 活跃标签（最多5个）
     */
    private List<TagWithCount> activeTags;
    
    /**
     * 最近帖子（最多3篇）
     */
    private List<CommunityPostResponse> recentPosts;
    
    @Data
    public static class UserStats {
        /**
         * 发帖数
         */
        private Integer postCount;
        
        /**
         * 获赞数
         */
        private Integer likeCount;
        
        /**
         * 评论数
         */
        private Integer commentCount;
        
        /**
         * 收藏数
         */
        private Integer collectCount;
    }
    
    @Data
    public static class TagWithCount {
        /**
         * 标签ID
         */
        private Long id;
        
        /**
         * 标签名称
         */
        private String name;
        
        /**
         * 标签颜色
         */
        private String color;
        
        /**
         * 使用次数
         */
        private Integer count;
    }
}

