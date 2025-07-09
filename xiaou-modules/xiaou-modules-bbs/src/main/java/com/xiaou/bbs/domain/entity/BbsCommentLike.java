package com.xiaou.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 评论点赞表，记录用户对评论的点赞行为
 * @TableName u_bbs_comment_like
 */
@TableName(value ="u_bbs_comment_like")
@Data
public class BbsCommentLike {
    /**
     * 主键ID
     */
    @TableId
    private String id;

    /**
     * 评论ID
     */
    private String commentId;

    /**
     * 点赞用户ID
     */
    private String userId;

    /**
     * 点赞时间
     */
    private Date createdAt;
}