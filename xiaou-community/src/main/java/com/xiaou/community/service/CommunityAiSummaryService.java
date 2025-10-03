package com.xiaou.community.service;

import java.util.Map;

/**
 * 社区AI摘要Service
 * 
 * @author xiaou
 */
public interface CommunityAiSummaryService {
    
    /**
     * 为帖子生成AI摘要
     * 
     * @param postId 帖子ID
     * @param forceRefresh 是否强制刷新
     * @return 摘要结果 {summary: "摘要", keywords: ["关键词1", "关键词2"]}
     */
    Map<String, Object> generateSummary(Long postId, boolean forceRefresh);
    
    /**
     * 获取帖子摘要
     * 
     * @param postId 帖子ID
     * @return 摘要结果，如果不存在返回null
     */
    Map<String, Object> getSummary(Long postId);
}

