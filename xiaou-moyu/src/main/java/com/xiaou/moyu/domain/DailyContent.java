package com.xiaou.moyu.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 每日内容实体
 *
 * @author xiaou
 */
@Data
public class DailyContent {
    
    /**
     * 内容ID
     */
    private Long id;
    
    /**
     * 内容类型：1-编程格言，2-技术小贴士，3-代码片段，4-历史上的今天
     */
    private Integer contentType;
    
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
     * 标签JSON字符串（业务层转换为List）
     */
    private String tags;
    
    /**
     * 难度等级：1-初级，2-中级，3-高级
     */
    private Integer difficultyLevel;
    
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
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
