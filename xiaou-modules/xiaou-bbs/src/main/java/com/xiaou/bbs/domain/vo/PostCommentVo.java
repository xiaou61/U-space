package com.xiaou.bbs.domain.vo;

import lombok.Data;

import java.util.Date;

@Data
public class PostCommentVo {

    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 父评论ID（0表示一级评论）
     */
    private Long parentId;

    /**
     * 评论者用户ID
     */
    private Long userId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 是否删除 0否 1是
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createTime;
}