package com.xiaou.community.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 社区标签实体
 * 
 * @author xiaou
 */
@Data
public class CommunityTag {
    
    /**
     * 标签ID
     */
    private Long id;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 标签描述
     */
    private String description;
    
    /**
     * 标签颜色
     */
    private String color;
    
    /**
     * 标签图标
     */
    private String icon;
    
    /**
     * 帖子数量
     */
    private Integer postCount;
    
    /**
     * 关注数量
     */
    private Integer followCount;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 状态：1-启用，0-禁用
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

