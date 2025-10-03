package com.xiaou.community.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.enums.CozeWorkflowEnum;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.CozeUtils;
import com.xiaou.common.utils.JsonUtils;
import com.xiaou.common.utils.RedisUtil;
import com.xiaou.community.config.CommunityProperties;
import com.xiaou.community.domain.CommunityPost;
import com.xiaou.community.mapper.CommunityPostMapper;
import com.xiaou.community.service.CommunityAiSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 社区AI摘要Service实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityAiSummaryServiceImpl implements CommunityAiSummaryService {
    
    private final CommunityPostMapper communityPostMapper;
    private final CommunityProperties communityProperties;
    private final CozeUtils cozeUtils;
    private final RedisUtil redisUtil;
    
    private static final String SUMMARY_CACHE_KEY = "community:post:summary:";
    
    @Override
    @Transactional
    public Map<String, Object> generateSummary(Long postId, boolean forceRefresh) {
        // 检查是否启用AI功能
        if (!communityProperties.getAi().getEnabled()) {
            throw new BusinessException("AI功能未启用");
        }
        
        // 查询帖子
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        
        String cacheKey = SUMMARY_CACHE_KEY + postId;
        
        // 如果不是强制刷新，先从缓存获取
        if (!forceRefresh) {
            String cachedData = redisUtil.get(cacheKey, String.class);
            if (cachedData != null) {
                log.info("从缓存获取帖子摘要，帖子ID: {}", postId);
                return JsonUtils.parseMap(cachedData);
            }
            
            // 缓存不存在，从数据库获取
            if (post.getAiSummary() != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("summary", post.getAiSummary());
                result.put("keywords", post.getAiKeywords() != null ? 
                    post.getAiKeywords().split(",") : new String[0]);
                
                // 回填缓存
                redisUtil.set(cacheKey, JsonUtils.toJsonString(result), 
                    communityProperties.getAi().getSummaryCacheTtl());
                
                log.info("从数据库获取帖子摘要，帖子ID: {}", postId);
                return result;
            }
        }
        
        // 调用Coze工作流生成摘要
        try {
            log.info("调用Coze工作流生成摘要，帖子ID: {}", postId);
            
            // 构建参数
            Map<String, Object> params = new HashMap<>();
            params.put("title", post.getTitle());
            params.put("content", post.getContent());
            
            // 调用工作流
            Result<String> cozeResult = cozeUtils.runWorkflow(
                CozeWorkflowEnum.COMMUNITY_POST_SUMMARY,
                params
            );
            
            if (!cozeResult.isSuccess()) {
                throw new BusinessException("AI摘要生成失败：" + cozeResult.getMessage());
            }
            
            // 解析结果
            String resultData = cozeResult.getData();
            JSONObject jsonResult = JsonUtils.toJSONObject(resultData);
            
            String summary = jsonResult.getString("summary");
            Object keywordsObj = jsonResult.get("keywords");
            String[] keywords;
            
            if (keywordsObj instanceof String) {
                // 如果是字符串，按逗号分割
                keywords = ((String) keywordsObj).split(",");
            } else {
                // 如果是数组，转换为String数组
                keywords = jsonResult.getList("keywords", String.class).toArray(new String[0]);
            }
            
            // 保存到数据库
            String keywordsStr = String.join(",", keywords);
            communityPostMapper.updateAiSummary(postId, summary, keywordsStr);
            
            // 保存到缓存
            Map<String, Object> result = new HashMap<>();
            result.put("summary", summary);
            result.put("keywords", keywords);
            
            redisUtil.set(cacheKey, JsonUtils.toJsonString(result), 
                communityProperties.getAi().getSummaryCacheTtl());
            
            log.info("AI摘要生成成功，帖子ID: {}", postId);
            return result;
            
        } catch (Exception e) {
            log.error("生成AI摘要失败，帖子ID: {}", postId, e);
            throw new BusinessException("生成AI摘要失败：" + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> getSummary(Long postId) {
        String cacheKey = SUMMARY_CACHE_KEY + postId;
        
        // 先从缓存获取
        String cachedData = redisUtil.get(cacheKey, String.class);
        if (cachedData != null) {
            return JsonUtils.parseMap(cachedData);
        }
        
        // 从数据库获取
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null || post.getAiSummary() == null) {
            return null;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("summary", post.getAiSummary());
        result.put("keywords", post.getAiKeywords() != null ? 
            post.getAiKeywords().split(",") : new String[0]);
        
        // 回填缓存
        redisUtil.set(cacheKey, JsonUtils.toJsonString(result), 
            communityProperties.getAi().getSummaryCacheTtl());
        
        return result;
    }
}

