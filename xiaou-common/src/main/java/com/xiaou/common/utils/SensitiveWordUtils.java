package com.xiaou.common.utils;

import com.xiaou.sensitive.api.SensitiveCheckService;
import com.xiaou.sensitive.api.dto.SensitiveCheckRequest;
import com.xiaou.sensitive.api.dto.SensitiveCheckResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 敏感词检测工具类 - 优化版
 * 直接调用Service，避免HTTP开销
 * 提供静态方法，保持向后兼容
 */
@Slf4j
@Component
public class SensitiveWordUtils {

    private static SensitiveCheckService sensitiveCheckService;
    
    // 缓存配置
    private static final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRE_TIME = TimeUnit.MINUTES.toMillis(5); // 5分钟缓存
    private static final int MAX_CACHE_SIZE = 500;
    
    // 性能保护参数
    private static final int MAX_TEXT_LENGTH = 10000; // 最大检测文本长度

    @Autowired
    public void setSensitiveCheckService(SensitiveCheckService sensitiveCheckService) {
        SensitiveWordUtils.sensitiveCheckService = sensitiveCheckService;
    }

    /**
     * 检测文本中的敏感词（带缓存）
     * @param text 待检测文本
     * @param module 模块名称
     * @param businessId 业务ID
     * @param userId 用户ID
     * @return 检测结果
     */
    public static SensitiveCheckResult checkText(String text, String module, Long businessId, Long userId) {
        if (text == null || text.trim().isEmpty()) {
            return SensitiveCheckResult.builder()
                    .hit(false)
                    .processedText(text)
                    .allowed(true)
                    .build();
        }

        if (sensitiveCheckService == null) {
            log.warn("敏感词检测服务未初始化，默认允许通过");
            return createDefaultResult(text);
        }

        // 文本长度保护
        String originalText = text;
        if (text.length() > MAX_TEXT_LENGTH) {
            log.warn("文本长度过长：{}，截取前{}个字符进行检测", text.length(), MAX_TEXT_LENGTH);
            text = text.substring(0, MAX_TEXT_LENGTH);
        }

        // 检查缓存
        String cacheKey = generateCacheKey(originalText, module);
        CacheEntry cachedResult = cache.get(cacheKey);
        if (cachedResult != null && !cachedResult.isExpired()) {
            log.debug("命中敏感词检测缓存：{}", cacheKey);
            return cachedResult.result;
        }

        try {
            // 构建请求参数
            SensitiveCheckRequest request = new SensitiveCheckRequest();
            request.setText(text);
            request.setModule(module);
            request.setBusinessId(businessId);
            request.setUserId(userId);

            // 直接调用Service
            SensitiveCheckResponse response = sensitiveCheckService.checkText(request);
            
            // 处理完整文本：如果原文本被截取，需要拼接未检测的部分
            String finalProcessedText = getProcessedText(response.getProcessedText(), originalText, text);
            
            SensitiveCheckResult result = SensitiveCheckResult.builder()
                    .hit(response.getHit())
                    .processedText(finalProcessedText)
                    .allowed(response.getAllowed())
                    .riskLevel(response.getRiskLevel())
                    .action(response.getAction())
                    .build();
            
            // 缓存结果
            cacheResult(cacheKey, result);
            return result;

        } catch (Exception e) {
            log.error("敏感词检测异常：{}", e.getMessage(), e);
            // 异常时默认允许通过，但记录日志
            SensitiveCheckResult defaultResult = createDefaultResult(originalText);
            // 异常情况下不缓存，避免缓存错误结果
            return defaultResult;
        }
    }

    /**
     * 检测文本是否包含敏感词
     * @param text 待检测文本
     * @param module 模块名称
     * @return 是否包含敏感词
     */
    public static boolean containsSensitiveWords(String text, String module) {
        if (sensitiveCheckService == null) {
            log.warn("敏感词检测服务未初始化，默认不包含敏感词");
            return false;
        }
        
        try {
            return sensitiveCheckService.containsSensitiveWords(text, module);
        } catch (Exception e) {
            log.error("检测敏感词异常：{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 替换文本中的敏感词
     * @param text 待处理文本
     * @param module 模块名称
     * @return 处理后的文本
     */
    public static String replaceSensitiveWords(String text, String module) {
        if (sensitiveCheckService == null) {
            log.warn("敏感词检测服务未初始化，返回原文本");
            return text;
        }
        
        try {
            return sensitiveCheckService.replaceSensitiveWords(text, module);
        } catch (Exception e) {
            log.error("替换敏感词异常：{}", e.getMessage(), e);
            return text;
        }
    }

    /**
     * 检测文本是否允许发布
     * @param text 待检测文本
     * @param module 模块名称
     * @param businessId 业务ID
     * @param userId 用户ID
     * @return 是否允许发布
     */
    public static boolean isAllowed(String text, String module, Long businessId, Long userId) {
        if (sensitiveCheckService == null) {
            log.warn("敏感词检测服务未初始化，默认允许发布");
            return true;
        }
        
        try {
            return sensitiveCheckService.isAllowed(text, module, businessId, userId);
        } catch (Exception e) {
            log.error("检测文本是否允许发布异常：{}", e.getMessage(), e);
            return true; // 异常时默认允许
        }
    }

    /**
     * 批量检测（优化版）
     * @param texts 待检测文本列表
     * @param module 模块名称
     * @return 检测结果列表
     */
    public static Map<String, SensitiveCheckResult> checkTextBatch(List<String> texts, String module) {
        Map<String, SensitiveCheckResult> results = new HashMap<>();
        
        if (texts == null || texts.isEmpty()) {
            return results;
        }
        
        if (sensitiveCheckService == null) {
            log.warn("敏感词检测服务未初始化，批量检测返回默认结果");
            for (String text : texts) {
                if (text != null) {
                    results.put(text, createDefaultResult(text));
                }
            }
            return results;
        }
        
        // 并行处理，提高性能
        texts.parallelStream().forEach(text -> {
            if (text != null) {
                SensitiveCheckResult result = checkText(text, module, null, null);
                synchronized (results) {
                    results.put(text, result);
                }
            }
        });
        
        return results;
    }

    /**
     * 清空缓存
     */
    public static void clearCache() {
        cache.clear();
        log.info("敏感词检测缓存已清空");
    }

    /**
     * 获取缓存统计信息
     */
    public static Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("size", cache.size());
        stats.put("maxSize", MAX_CACHE_SIZE);
        stats.put("expireTime", CACHE_EXPIRE_TIME);
        return stats;
    }

    /**
     * 生成缓存Key
     */
    private static String generateCacheKey(String text, String module) {
        return String.format("%s:%s:%d", module, text.hashCode(), text.length());
    }

    /**
     * 缓存结果
     */
    private static void cacheResult(String key, SensitiveCheckResult result) {
        // 缓存大小控制
        if (cache.size() >= MAX_CACHE_SIZE) {
            // 简单的LRU实现：删除最老的缓存项
            String oldestKey = cache.keySet().iterator().next();
            cache.remove(oldestKey);
        }
        
        cache.put(key, new CacheEntry(result, System.currentTimeMillis()));
    }

    /**
     * 处理响应文本
     */
    private static String getProcessedText(String backendProcessedText, String originalText, String truncatedText) {
        if (backendProcessedText == null) {
            return originalText;
        }
        
        // 如果原文本被截取了，需要拼接未检测的部分
        if (originalText.length() > truncatedText.length()) {
            String remainingText = originalText.substring(truncatedText.length());
            return backendProcessedText + remainingText;
        }
        
        return backendProcessedText;
    }

    /**
     * 创建默认结果（异常情况下使用）
     */
    private static SensitiveCheckResult createDefaultResult(String text) {
        return SensitiveCheckResult.builder()
                .hit(false)
                .processedText(text)
                .allowed(true)
                .riskLevel(0)
                .action("none")
                .build();
    }

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        final SensitiveCheckResult result;
        final long timestamp;
        
        CacheEntry(SensitiveCheckResult result, long timestamp) {
            this.result = result;
            this.timestamp = timestamp;
        }
        
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRE_TIME;
        }
    }

    /**
     * 敏感词检测结果 - 保持与原版兼容
     */
    public static class SensitiveCheckResult {
        private Boolean hit;
        private String processedText;
        private Boolean allowed;
        private Integer riskLevel;
        private String action;

        // 构造器
        public SensitiveCheckResult() {}

        public SensitiveCheckResult(Boolean hit, String processedText, Boolean allowed, Integer riskLevel, String action) {
            this.hit = hit;
            this.processedText = processedText;
            this.allowed = allowed;
            this.riskLevel = riskLevel;
            this.action = action;
        }

        // Builder 模式
        public static SensitiveCheckResultBuilder builder() {
            return new SensitiveCheckResultBuilder();
        }

        public static class SensitiveCheckResultBuilder {
            private Boolean hit;
            private String processedText;
            private Boolean allowed;
            private Integer riskLevel;
            private String action;

            public SensitiveCheckResultBuilder hit(Boolean hit) {
                this.hit = hit;
                return this;
            }

            public SensitiveCheckResultBuilder processedText(String processedText) {
                this.processedText = processedText;
                return this;
            }

            public SensitiveCheckResultBuilder allowed(Boolean allowed) {
                this.allowed = allowed;
                return this;
            }

            public SensitiveCheckResultBuilder riskLevel(Integer riskLevel) {
                this.riskLevel = riskLevel;
                return this;
            }

            public SensitiveCheckResultBuilder action(String action) {
                this.action = action;
                return this;
            }

            public SensitiveCheckResult build() {
                return new SensitiveCheckResult(hit, processedText, allowed, riskLevel, action);
            }
        }

        // Getters
        public Boolean getHit() { return hit; }
        public String getProcessedText() { return processedText; }
        public Boolean getAllowed() { return allowed; }
        public Integer getRiskLevel() { return riskLevel; }
        public String getAction() { return action; }

        // Setters
        public void setHit(Boolean hit) { this.hit = hit; }
        public void setProcessedText(String processedText) { this.processedText = processedText; }
        public void setAllowed(Boolean allowed) { this.allowed = allowed; }
        public void setRiskLevel(Integer riskLevel) { this.riskLevel = riskLevel; }
        public void setAction(String action) { this.action = action; }
    }
} 