package com.xiaou.community.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 社区评论实体
 * 
 * @author xiaou
 */
@Data
public class CommunityComment {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 父评论ID，0表示顶级评论
     */
    private Long parentId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 评论者用户ID
     */
    private Long authorId;
    
    /**
     * 评论者用户名
     */
    private String authorName;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 状态：1-正常，2-删除
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