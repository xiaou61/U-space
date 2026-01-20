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
import com.xiaou.community.service.CommunityCacheService;
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
    private final CommunityCacheService communityCacheService;
    
    private static final String SUMMARY_CACHE_KEY = "community:post:summary:";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
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
            
            // 🔍 打印原始返回数据，方便调试
            log.info("======== Coze工作流原始返回数据 START ========");
            log.info("帖子ID: {}", postId);
            log.info("原始数据类型: {}", resultData != null ? resultData.getClass().getName() : "null");
            log.info("原始数据内容: {}", resultData);
            log.info("======== Coze工作流原始返回数据 END ========");
            
            if (resultData == null || resultData.trim().isEmpty()) {
                log.error("Coze工作流返回数据为空，帖子ID: {}", postId);
                throw new BusinessException("AI摘要生成失败：工作流返回数据为空，请检查Coze工作流配置和权限");
            }
            
            JSONObject jsonResult = null;
            try {
                jsonResult = JsonUtils.toJSONObject(resultData);
                // 🔍 打印解析后的JSON对象
                log.info("解析后的JSON对象: {}", jsonResult);
            } catch (Exception e) {
                log.error("解析Coze工作流返回数据失败，帖子ID: {}，返回数据: {}", postId, resultData, e);
                throw new BusinessException("AI摘要生成失败：工作流返回数据格式错误");
            }
            
            if (jsonResult == null) {
                log.error("解析Coze工作流返回的JSON为null，帖子ID: {}，返回数据: {}", postId, resultData);
                throw new BusinessException("AI摘要生成失败：无法解析工作流返回数据");
            }
            
            // 🔥 Coze工作流返回的数据结构是 {"output": "{\"summary\":\"...\",\"keywords\":[...]}"} 
            // 需要先取出output字段，再二次解析
            String outputStr = jsonResult.getString("output");
            if (outputStr == null || outputStr.trim().isEmpty()) {
                log.error("Coze工作流返回的output字段为空，帖子ID: {}，返回数据: {}", postId, resultData);
                throw new BusinessException("AI摘要生成失败：工作流返回的output字段为空");
            }
            
            // 二次解析output字段
            JSONObject actualResult = null;
            try {
                actualResult = JsonUtils.toJSONObject(outputStr);
                log.info("二次解析后的实际数据: {}", actualResult);
            } catch (Exception e) {
                log.error("二次解析output字段失败，帖子ID: {}，output内容: {}", postId, outputStr, e);
                throw new BusinessException("AI摘要生成失败：output字段格式错误");
            }
            
            if (actualResult == null) {
                log.error("二次解析output后为null，帖子ID: {}，output内容: {}", postId, outputStr);
                throw new BusinessException("AI摘要生成失败：无法解析output内容");
            }
            
            String summary = actualResult.getString("summary");
            if (summary == null || summary.trim().isEmpty()) {
                log.error("摘要内容为空，帖子ID: {}，实际数据: {}", postId, actualResult);
                throw new BusinessException("AI摘要生成失败：摘要内容为空");
            }
            
            // 处理关键词
            String[] keywords = new String[0];
            Object keywordsObj = actualResult.get("keywords");
            
            if (keywordsObj != null) {
                if (keywordsObj instanceof String) {
                    // 如果是字符串，按逗号分割
                    String keywordsStr = ((String) keywordsObj).trim();
                    if (!keywordsStr.isEmpty()) {
                        keywords = keywordsStr.split(",\\s*");
                    }
                } else {
                    // 如果是数组，转换为String数组
                    try {
                        java.util.List<String> keywordsList = actualResult.getList("keywords", String.class);
                        if (keywordsList != null && !keywordsList.isEmpty()) {
                            keywords = keywordsList.toArray(new String[0]);
                        }
                    } catch (Exception e) {
                        log.warn("解析关键词数组失败，帖子ID: {}", postId, e);
                    }
                }
            } else {
                log.warn("AI返回的关键词为空，帖子ID: {}", postId);
            }
            
            // 保存到数据库
            String keywordsStr = String.join(",", keywords);
            communityPostMapper.updateAiSummary(postId, summary, keywordsStr);
            
            // 删除帖子详情缓存，确保下次查询时返回最新数据
            communityCacheService.evictPost(postId);
            
            // 保存到AI摘要缓存
            Map<String, Object> result = new HashMap<>();
            result.put("summary", summary);
            result.put("keywords", keywords);
            
            redisUtil.set(cacheKey, JsonUtils.toJsonString(result), 
                communityProperties.getAi().getSummaryCacheTtl());
            
            log.info("AI摘要生成成功并已清除帖子缓存，帖子ID: {}", postId);
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

