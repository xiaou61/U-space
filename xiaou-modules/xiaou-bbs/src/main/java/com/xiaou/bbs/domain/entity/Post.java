package com.xiaou.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 帖子表
 */
@TableName(value = "u_post")
@Data
public class Post {
    /**
     * 帖子ID
     */
    @TableId
    private Long id;

    /**
     * 作者用户ID
     */
    private Long userId;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 状态（1:正常，0:禁用）
     */
    private Integer status;

    /**
     * 是否已删除（0：未删除，1：已删除）
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 图标地址
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> imageUrls;
}