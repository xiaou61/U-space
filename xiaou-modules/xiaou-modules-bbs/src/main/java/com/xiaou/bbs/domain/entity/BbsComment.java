package com.xiaou.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 帖子一级评论表
 * @TableName u_bbs_comment
 */
@TableName(value ="u_bbs_comment")
@Data
public class BbsComment {
    /**
     * 评论ID，UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 所属帖子ID
     */
    private String postId;

    /**
     * 评论用户ID
     */
    private String userId;

    /**
     * 评论内容
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