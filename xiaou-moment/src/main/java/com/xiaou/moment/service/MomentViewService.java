package com.xiaou.moment.service;

/**
 * 动态浏览统计Service接口
 */
public interface MomentViewService {
    
    /**
     * 记录浏览（防重复统计：同一用户5分钟内只统计一次）
     */
    void recordView(Long momentId, Long userId);
    
    /**
     * 获取动态浏览数（从Redis）
     */
    Integer getViewCount(Long momentId);
    
    /**
     * 同步浏览数到数据库（定时任务调用）
     */
    void syncViewCountToDatabase();
}

