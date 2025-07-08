package com.xiaou.bbs.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 帖子表：记录校园BBS的帖子内容，关联帖子分类
 * @TableName u_bbs_post
 */
@TableName(value ="u_bbs_post")
@Data
public class BbsPost {
    /**
     * 帖子ID，UUID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 发布人ID，关联用户表
     */
    private String userId;

    /**
     * 帖子分类ID，关联 u_bbs_category 表
     */
    private String categoryId;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 帖子图片URL数组，JSON格式
     */
    private List<String> images;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 是否置顶：0否 1是
     */
    private Integer isTop;

    /**
     * 是否推荐：0否 1是（由后台定时任务更新）
     */
    private Integer isRecommend;

    /**
     * 帖子状态：1正常，0禁用
     */
    private Integer status;

    /**
     * 逻辑删除：0正常，1已删除
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}