package com.xiaou.blog.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 博客文章实体
 * 
 * @author xiaou
 */
@Data
public class BlogArticle {
    
    /**
     * 文章ID
     */
    private Long id;
    
    /**
     * 作者用户ID
     */
    private Long userId;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 文章封面图
     */
    private String coverImage;
    
    /**
     * 文章摘要
     */
    private String summary;
    
    /**
     * 文章内容（Markdown格式）
     */
    private String content;
    
    /**
     * 文章分类ID
     */
    private Long categoryId;
    
    /**
     * 文章标签，JSON数组格式
     */
    private String tags;
    
    /**
     * 文章状态：0-草稿 1-已发布 2-已下架 3-已删除
     */
    private Integer status;
    
    /**
     * 是否原创：1-原创 0-转载
     */
    private Integer isOriginal;
    
    /**
     * 是否置顶：1-置顶 0-普通
     */
    private Integer isTop;
    
    /**
     * 置顶过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date topExpireTime;
    
    /**
     * 发布消耗积分
     */
    private Integer pointsCost;
    
    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    
    // ========== 扩展字段（非数据库字段） ==========
    
    /**
     * 作者昵称
     */
    private String userNickname;
    
    /**
     * 作者头像
     */
    private String userAvatar;
    
    /**
     * 分类名称
     */
    private String categoryName;
}

