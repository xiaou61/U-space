package com.xiaou.blog.service;

import com.xiaou.blog.domain.BlogConfig;
import com.xiaou.blog.dto.BlogCheckStatusResponse;
import com.xiaou.blog.dto.BlogConfigResponse;
import com.xiaou.blog.dto.BlogConfigUpdateRequest;
import com.xiaou.blog.dto.BlogOpenResponse;
import com.xiaou.blog.dto.BlogStatisticsResponse;

/**
 * 博客配置Service接口
 * 
 * @author xiaou
 */
public interface BlogConfigService {
    
    /**
     * 开通博客（扣除50积分）
     */
    BlogOpenResponse openBlog();
    
    /**
     * 检查博客开通状态
     */
    BlogCheckStatusResponse checkBlogStatus();
    
    /**
     * 根据用户ID获取博客配置
     */
    BlogConfigResponse getBlogConfigByUserId(Long userId);
    
    /**
     * 更新博客配置
     */
    void updateBlogConfig(BlogConfigUpdateRequest request);
    
    /**
     * 根据用户ID查询博客配置实体
     */
    BlogConfig getBlogByUserId(Long userId);
    
    /**
     * 更新文章总数
     */
    void updateTotalArticles(Long userId, Integer increment);
    
    /**
     * 获取博客统计数据
     */
    BlogStatisticsResponse getStatistics();
}

