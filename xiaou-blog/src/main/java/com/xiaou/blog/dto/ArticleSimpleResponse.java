package com.xiaou.blog.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 文章简单信息响应DTO
 * 
 * @author xiaou
 */
@Data
public class ArticleSimpleResponse {
    
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
     * 是否置顶
     */
    private Integer isTop;
    
    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishTime;
}


