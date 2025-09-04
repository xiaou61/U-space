package com.xiaou.community.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 用户社区状态实体
 * 
 * @author xiaou
 */
@Data
public class CommunityUserStatus {
    
    /**
     * 状态ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 是否封禁：0-否，1-是
     */
    private Integer isBanned;
    
    /**
     * 封禁原因
     */
    private String banReason;
    
    /**
     * 封禁过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date banExpireTime;
    
    /**
     * 发帖数
     */
    private Integer postCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 收藏数
     */
    private Integer collectCount;
    
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