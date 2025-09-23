package com.xiaou.version.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 版本更新历史实体
 * 
 * @author xiaou
 */
@Data
public class VersionHistory {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 版本号
     */
    private String versionNumber;
    
    /**
     * 更新标题
     */
    private String title;
    
    /**
     * 更新类型: 1-重大更新, 2-功能更新, 3-修复更新, 4-其他
     */
    private Integer updateType;
    
    /**
     * 版本更新简要描述
     */
    private String description;
    
    /**
     * PRD文档链接
     */
    private String prdUrl;
    
    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date releaseTime;
    
    /**
     * 状态: 0-草稿, 1-已发布, 2-已隐藏
     */
    private Integer status;
    
    /**
     * 排序权重(数字越大越靠前)
     */
    private Integer sortOrder;
    
    /**
     * 查看次数
     */
    private Integer viewCount;
    
    /**
     * 是否重点推荐: 0-否, 1-是
     */
    private Integer isFeatured;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdTime;
    
    /**
     * 更新人ID
     */
    private Long updatedBy;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedTime;
    
    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    private Integer deleted;
} 