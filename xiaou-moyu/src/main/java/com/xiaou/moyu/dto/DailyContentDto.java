package com.xiaou.moyu.dto;

import lombok.Data;

import java.util.List;

/**
 * 每日内容响应对象
 *
 * @author xiaou
 */
@Data
public class DailyContentDto {
    
    /**
     * 内容ID
     */
    private Long id;
    
    /**
     * 内容类型：1-编程格言，2-技术小贴士，3-代码片段，4-历史上的今天
     */
    private Integer contentType;
    
    /**
     * 内容类型名称
     */
    private String contentTypeName;
    
    /**
     * 内容标题
     */
    private String title;
    
    /**
     * 内容正文
     */
    private String content;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 相关编程语言
     */
    private String programmingLanguage;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 难度等级：1-初级，2-中级，3-高级
     */
    private Integer difficultyLevel;
    
    /**
     * 难度等级名称
     */
    private String difficultyLevelName;
    
    /**
     * 来源链接
     */
    private String sourceUrl;
    
    /**
     * 查看次数
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 是否已收藏
     */
    private Boolean isCollected;
}
