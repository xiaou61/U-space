package com.xiaou.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 评论回复表（二级评论）
 * @TableName u_bbs_comment_reply
 */
@TableName(value ="u_bbs_comment_reply")
@Data
public class BbsCommentReply {
    /**
     * 回复ID，UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 所属评论ID
     */
    private String commentId;

    /**
     * 所属帖子ID，便于反查
     */
    private String postId;

    /**
     * 回复人ID
     */
    private String userId;

    /**
     * 被回复的用户ID（@）
     */
    private String replyUserId;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 逻辑删除：0正常，1删除
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
}