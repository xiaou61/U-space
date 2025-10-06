package com.xiaou.blog.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 文章分类实体
 * 
 * @author xiaou
 */
@Data
public class BlogCategory {
    
    /**
     * 分类ID
     */
    private Long id;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 分类图标URL
     */
    private String categoryIcon;
    
    /**
     * 分类描述
     */
    private String categoryDescription;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
    
    /**
     * 文章数量
     */
    private Integer articleCount;
    
    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;
    
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
}

