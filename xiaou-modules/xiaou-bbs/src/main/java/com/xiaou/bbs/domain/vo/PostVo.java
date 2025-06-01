package com.xiaou.bbs.domain.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 帖子表Vo
 */
@Data
public class PostVo {
    /**
     * 帖子ID
     */
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 图标地址
     */
    private List<String> imageUrls;

    /**
     * 分类
     */
    private String category;

    // ✅ 新增字段：作者昵称、头像
    private String nickname;
    private String avatarUrl;
}