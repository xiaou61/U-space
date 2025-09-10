package com.xiaou.sensitive.service.impl;

import cn.hutool.json.JSONUtil;
import com.xiaou.sensitive.api.SensitiveCheckService;
import com.xiaou.sensitive.api.dto.SensitiveCheckRequest;
import com.xiaou.sensitive.api.dto.SensitiveCheckResponse;
import com.xiaou.sensitive.engine.SensitiveEngine;
import com.xiaou.sensitive.mapper.SensitiveLogMapper;
import com.xiaou.sensitive.mapper.SensitiveWordMapper;
import com.xiaou.sensitive.domain.SensitiveLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 敏感词检测服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveCheckServiceImpl implements SensitiveCheckService {

    private final SensitiveEngine sensitiveEngine;
    private final SensitiveWordMapper sensitiveWordMapper;
    private final SensitiveLogMapper sensitiveLogMapper;
    
    // 性能保护参数
    private static final int MAX_TEXT_LENGTH = 10000; // 最大文本长度
    private static final int MAX_BATCH_SIZE = 100; // 最大批量处理数量
    private static final long MAX_PROCESSING_TIME = 5000; // 最大处理时间（毫秒）
    
    // 线程池用于批量处理
    private ExecutorService batchExecutor;

    @PostConstruct
    public void init() {
        // 初始化线程池
        batchExecutor = new ThreadPoolExecutor(
                2, // 核心线程数
                8, // 最大线程数
                60L, TimeUnit.SECONDS, // 空闲时间
                new LinkedBlockingQueue<>(200), // 队列大小
                new ThreadFactory() {
                    private int counter = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "SensitiveCheck-" + (++counter));
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
        
        refreshWordLibrary();
    }
    
    @PreDestroy
    public void destroy() {
        if (batchExecutor != null && !batchExecutor.isShutdown()) {
            batchExecutor.shutdown();
            try {
                if (!batchExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    batchExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                batchExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public SensitiveCheckResponse checkText(SensitiveCheckRequest request) {
        if (request == null || request.getText() == null || request.getText().trim().isEmpty()) {
            return SensitiveCheckResponse.builder()
                    .hit(false)
                    .hitWords(new ArrayList<>())
                    .processedText(request != null ? request.getText() : "")
                    .riskLevel(0)
                    .action("none")
                    .allowed(true)
                    .build();
        }

        try {
            String text = request.getText().trim();
            
            // 文本长度保护
            if (text.length() > MAX_TEXT_LENGTH) {
                log.warn("文本长度过长：{}，截取前{}个字符进行检测", text.length(), MAX_TEXT_LENGTH);
                text = text.substring(0, MAX_TEXT_LENGTH);
                // 更新请求对象
                request.setText(text);
            }
            
            // 添加处理超时控制
            long startTime = System.currentTimeMillis();
            
            // 检测敏感词
            List<String> hitWords = sensitiveEngine.findSensitiveWords(text);
            boolean hit = !hitWords.isEmpty();
            
            // 检查处理时间
            long processingTime = System.currentTimeMillis() - startTime;
            if (processingTime > MAX_PROCESSING_TIME) {
                log.warn("敏感词检测耗时过长：{}ms，文本长度：{}", processingTime, text.length());
            }
            
            SensitiveCheckResponse.SensitiveCheckResponseBuilder responseBuilder = SensitiveCheckResponse.builder()
                    .hit(hit)
                    .hitWords(hitWords);

            if (hit) {
                // 计算风险等级（简化逻辑：根据命中敏感词数量）
                int riskLevel = calculateRiskLevel(hitWords.size());
                
                // 确定处理动作
                String action = determineAction(riskLevel);
                
                // 处理文本
                String processedText = processText(text, action);
                
                // 确定是否允许通过
                boolean allowed = isAllowed(action);
                
                responseBuilder
                        .processedText(processedText)
                        .riskLevel(riskLevel)
                        .action(action)
                        .allowed(allowed);

                // 异步记录日志，避免影响主流程性能
                logSensitiveDetectionAsync(request, hitWords, action);
            } else {
                responseBuilder
                        .processedText(text)
                        .riskLevel(0)
                        .action("none")
                        .allowed(true);
            }

            return responseBuilder.build();
            
        } catch (Exception e) {
            log.error("敏感词检测异常：{}", e.getMessage(), e);
            return SensitiveCheckResponse.builder()
                    .hit(false)
                    .hitWords(new ArrayList<>())
                    .processedText(request.getText())
                    .riskLevel(0)
                    .action("error")
                    .allowed(false)
                    .build();
        }
    }

    @Override
    public List<SensitiveCheckResponse> checkTextBatch(List<SensitiveCheckRequest> requests) {
        List<SensitiveCheckResponse> responses = new ArrayList<>();
        
        if (requests == null || requests.isEmpty()) {
            return responses;
        }

        // 批量大小保护
        if (requests.size() > MAX_BATCH_SIZE) {
            log.warn("批量检测数量过多：{}，限制为：{}", requests.size(), MAX_BATCH_SIZE);
            requests = requests.subList(0, MAX_BATCH_SIZE);
        }

        // 对于小批量，直接串行处理
        if (requests.size() <= 10) {
            for (SensitiveCheckRequest request : requests) {
                responses.add(checkText(request));
            }
            return responses;
        }

        // 大批量使用并行处理
        try {
            List<Future<SensitiveCheckResponse>> futures = new ArrayList<>();
            
            for (SensitiveCheckRequest request : requests) {
                Future<SensitiveCheckResponse> future = batchExecutor.submit(() -> checkText(request));
                futures.add(future);
            }
            
            // 收集结果，设置超时
            for (Future<SensitiveCheckResponse> future : futures) {
                try {
                    SensitiveCheckResponse response = future.get(10, TimeUnit.SECONDS);
                    responses.add(response);
                } catch (TimeoutException e) {
                    log.warn("批量检测超时，使用默认结果");
                    responses.add(createDefaultResponse());
                    future.cancel(true);
                } catch (Exception e) {
                    log.error("批量检测异常：{}", e.getMessage());
                    responses.add(createDefaultResponse());
                }
            }
            
        } catch (Exception e) {
            log.error("批量检测服务异常：{}", e.getMessage(), e);
            // 异常情况下退回到串行处理
            responses.clear();
            for (SensitiveCheckRequest request : requests) {
                try {
                    responses.add(checkText(request));
                } catch (Exception ex) {
                    log.error("串行检测异常：{}", ex.getMessage());
                    responses.add(createDefaultResponse());
                }
            }
        }

        return responses;
    }

    @Override
    public boolean containsSensitiveWords(String text, String module) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        SensitiveCheckRequest request = new SensitiveCheckRequest();
        request.setText(text);
        request.setModule(module);
        
        SensitiveCheckResponse response = checkText(request);
        return response.getHit();
    }

    @Override
    public String replaceSensitiveWords(String text, String module) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        SensitiveCheckRequest request = new SensitiveCheckRequest();
        request.setText(text);
        request.setModule(module);
        
        SensitiveCheckResponse response = checkText(request);
        return response.getProcessedText();
    }

    @Override
    public boolean isAllowed(String text, String module, Long businessId, Long userId) {
        if (text == null || text.trim().isEmpty()) {
            return true;
        }
        
        SensitiveCheckRequest request = new SensitiveCheckRequest();
        request.setText(text);
        request.setModule(module);
        request.setBusinessId(businessId);
        request.setUserId(userId);
        
        SensitiveCheckResponse response = checkText(request);
        return response.getAllowed();
    }

    @Override
    public void refreshWordLibrary() {
        try {
            long startTime = System.currentTimeMillis();
            List<String> enabledWords = sensitiveWordMapper.selectEnabledWords();
            Set<String> wordSet = new HashSet<>(enabledWords != null ? enabledWords : new ArrayList<>());
            
            // 过滤无效词汇
            wordSet.removeIf(word -> word == null || word.trim().isEmpty() || word.length() > 100);
            
            sensitiveEngine.refresh(wordSet);
            long refreshTime = System.currentTimeMillis() - startTime;
            
            log.info("敏感词库刷新完成，加载词汇数量: {}，耗时: {}ms", wordSet.size(), refreshTime);
            if (wordSet.isEmpty()) {
                log.warn("敏感词库为空，请检查数据库中是否有启用的敏感词数据");
            }
        } catch (Exception e) {
            log.error("刷新敏感词库失败：{}", e.getMessage(), e);
            // 初始化失败时使用空词库，确保服务能正常启动
            try {
                sensitiveEngine.refresh(new HashSet<>());
                log.warn("敏感词库初始化失败，使用空词库启动服务");
            } catch (Exception ex) {
                log.error("敏感词引擎初始化失败：{}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * 计算风险等级
     * @param hitWordsCount 命中敏感词数量
     * @return 风险等级 1-低 2-中 3-高
     */
    private int calculateRiskLevel(int hitWordsCount) {
        if (hitWordsCount == 1) {
            return 1; // 低风险
        } else if (hitWordsCount <= 3) {
            return 2; // 中风险
        } else {
            return 3; // 高风险
        }
    }

    /**
     * 确定处理动作
     * @param riskLevel 风险等级
     * @return 处理动作
     */
    private String determineAction(int riskLevel) {
        switch (riskLevel) {
            case 1:
                return "replace"; // 替换
            case 2:
                return "review";  // 审核
            case 3:
                return "reject";  // 拒绝
            default:
                return "none";
        }
    }

    /**
     * 处理文本
     * @param text 原始文本
     * @param action 处理动作
     * @return 处理后文本
     */
    private String processText(String text, String action) {
        try {
            switch (action) {
                case "replace":
                    return sensitiveEngine.replaceSensitiveWords(text, "***");
                case "reject":
                    return ""; // 拒绝时返回空文本
                default:
                    return text; // 审核时返回原文本
            }
        } catch (Exception e) {
            log.error("文本处理异常：{}", e.getMessage());
            return text; // 异常时返回原文本
        }
    }

    /**
     * 判断是否允许通过
     * @param action 处理动作
     * @return 是否允许
     */
    private boolean isAllowed(String action) {
        return !"reject".equals(action);
    }

    /**
     * 异步记录敏感词检测日志
     */
    private void logSensitiveDetectionAsync(SensitiveCheckRequest request, List<String> hitWords, String action) {
        batchExecutor.submit(() -> {
            try {
                SensitiveLog log = new SensitiveLog();
                log.setUserId(request.getUserId());
                log.setModule(request.getModule());
                log.setBusinessId(request.getBusinessId());
                log.setOriginalText(request.getText());
                log.setHitWords(JSONUtil.toJsonStr(hitWords));
                log.setAction(action);
                log.setCreateTime(LocalDateTime.now());
                
                sensitiveLogMapper.insertLog(log);
            } catch (Exception e) {
                // 日志记录失败不影响主流程
                SensitiveCheckServiceImpl.log.warn("记录敏感词检测日志失败：{}", e.getMessage());
            }
        });
    }

    /**
     * 创建默认响应（异常情况下使用）
     */
    private SensitiveCheckResponse createDefaultResponse() {
        return SensitiveCheckResponse.builder()
                .hit(false)
                .hitWords(new ArrayList<>())
                .processedText("")
                .riskLevel(0)
                .action("error")
                .allowed(false)
                .build();
    }
} 