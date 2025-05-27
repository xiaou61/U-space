package com.xiaou.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@TableName(value ="u_post_comment")
@Data
public class PostComment {
    /**
     * 评论ID
     */
    @TableId
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
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}