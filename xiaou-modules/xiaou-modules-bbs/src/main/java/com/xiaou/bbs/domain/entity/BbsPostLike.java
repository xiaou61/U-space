package com.xiaou.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 帖子点赞表：记录用户对帖子的点赞/取消点赞行为
 * @TableName u_bbs_post_like
 */
@TableName(value ="u_bbs_post_like")
@Data
public class BbsPostLike {
    /**
     * 点赞记录ID，UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 帖子ID，关联 u_bbs_post 表
     */
    private String postId;

    /**
     * 用户ID，点赞人，关联用户表
     */
    private String userId;


    /**
     * 点赞时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}