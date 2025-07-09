package com.xiaou.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 帖子评论表，支持一级评论和对评论的回复（两级结构）
 * @TableName u_bbs_comment
 */
@TableName(value ="u_bbs_comment")
@Data
public class BbsComment {
    /**
     * 评论ID，UUID
     */
    @TableId
    private String id;

    /**
     * 所属帖子ID
     */
    private String postId;

    /**
     * 评论发布者用户ID
     */
    private String userId;

    /**
     * 评论内容，支持 @xxx 格式
     */
    private String content;

    /**
     * 父评论ID，null 表示一级评论，有值表示回复评论
     */
    private String parentId;

    /**
     * 被回复的用户ID，用于前端展示 @xxx
     */
    private String replyUserId;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 逻辑删除：0正常，1已删除
     */
    private Integer isDeleted;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}