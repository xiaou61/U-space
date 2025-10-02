package com.xiaou.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 文章详情响应DTO
 * 
 * @author xiaou
 */
@Data
public class ArticleDetailResponse {
    
    /**
     * 文章ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
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
     * 文章内容
     */
    private String content;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 文章标签
     */
    private List<String> tags;
    
    /**
     * 是否原创
     */
    private Integer isOriginal;
    
    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    
    /**
     * 是否可编辑
     */
    private Boolean canEdit;
    
    /**
     * 是否可删除
     */
    private Boolean canDelete;
    
    /**
     * 相关文章
     */
    private List<ArticleSimpleResponse> relatedArticles;
}


