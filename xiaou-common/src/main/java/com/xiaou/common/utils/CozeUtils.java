package com.xiaou.common.utils;


import com.coze.openapi.client.workflows.run.RunWorkflowReq;
import com.coze.openapi.client.workflows.run.model.WorkflowEvent;
import com.coze.openapi.client.workflows.run.model.WorkflowEventType;
import com.coze.openapi.service.auth.TokenAuth;
import com.coze.openapi.service.service.CozeAPI;
import com.xiaou.common.config.CozeConfig;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.enums.CozeWorkflowEnum;
import io.reactivex.Flowable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.coze.openapi.client.workflows.run.model.WorkflowEventType.*;

/**
 * Coze工具类
 * 提供Coze平台工作流调用的统一工具方法
 *
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor

public class CozeUtils {

    private final CozeConfig cozeConfig;
    private final RedisUtil redisUtil;

    /**
     * Coze API客户端
     */
    private CozeAPI cozeClient;


    /**
     * 初始化Coze客户端
     */
    @PostConstruct
    public void initCozeClient() {
        try {
            if (!StringUtils.hasText(cozeConfig.getAuth().getApiKey())) {
                log.warn("Coze API密钥未配置，CozeUtils将不可用");
                return;
            }

            // 创建认证客户端
            TokenAuth authClient = new TokenAuth(cozeConfig.getAuth().getApiKey());

            // 初始化Coze客户端
            this.cozeClient = new CozeAPI.Builder()
                    .baseURL(cozeConfig.getApi().getBaseUrl())
                    .auth(authClient)
                    .connectTimeout(cozeConfig.getApi().getConnectTimeout())
                    .readTimeout(cozeConfig.getApi().getReadTimeout())
                    .build();

            log.info("Coze客户端初始化成功");
        } catch (Exception e) {
            log.error("Coze客户端初始化失败", e);
        }
    }

    /**
     * 运行工作流（同步方式）
     *
     * @param workflowEnum 工作流枚举
     * @param parameters   工作流参数
     * @return 执行结果
     */
    public Result<String> runWorkflow(CozeWorkflowEnum workflowEnum, Map<String, Object> parameters) {
        return runWorkflow(workflowEnum.getWorkflowId(), parameters, false);
    }

    /**
     * 运行工作流（指定工作流ID）
     *
     * @param workflowId 工作流ID
     * @param parameters 工作流参数
     * @return 执行结果
     */
    public Result<String> runWorkflow(String workflowId, Map<String, Object> parameters) {
        return runWorkflow(workflowId, parameters, false);
    }

    /**
     * 运行工作流（支持缓存）
     *
     * @param workflowId  工作流ID
     * @param parameters  工作流参数
     * @param useCache    是否使用缓存
     * @return 执行结果
     */
    public Result<String> runWorkflow(String workflowId, Map<String, Object> parameters, boolean useCache) {
        try {
            // 参数校验
            if (!StringUtils.hasText(workflowId)) {
                return Result.error("工作流ID不能为空");
            }

            if (cozeClient == null) {
                return Result.error("Coze客户端未初始化");
            }

            // 检查缓存
            String cacheKey = generateCacheKey(workflowId, parameters);
            if (useCache && cozeConfig.getCache().getEnabled()) {
                String cachedResult = (String) redisUtil.get(cacheKey);
                if (StringUtils.hasText(cachedResult)) {
                    log.debug("从缓存获取工作流结果: {}", workflowId);
                    return Result.success(cachedResult);
                }
            }

            // 构建请求参数
            Map<String, Object> requestParams = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
            
            RunWorkflowReq request = RunWorkflowReq.builder()
                    .workflowID(workflowId)
                    .parameters(requestParams)
                    .build();

            // 执行工作流
            StringBuilder resultBuilder = new StringBuilder();
            io.reactivex.Flowable<WorkflowEvent> flowable = cozeClient.workflows().runs().stream(request);
            
            flowable.blockingForEach(event -> {
                handleWorkflowEvent(event, resultBuilder);
            });

            String result = resultBuilder.toString();

            // 缓存结果
            if (useCache && cozeConfig.getCache().getEnabled() && StringUtils.hasText(result)) {
                redisUtil.set(cacheKey, result, cozeConfig.getCache().getTtl());
            }

            log.info("工作流执行完成: {}", workflowId);
            return Result.success(result);

        } catch (Exception e) {
            log.error("工作流执行失败: workflowId={}, parameters={}", workflowId, parameters, e);
            return Result.error("工作流执行失败: " + e.getMessage());
        }
    }

    /**
     * 异步运行工作流
     *
     * @param workflowEnum 工作流枚举
     * @param parameters   工作流参数
     * @param callback     回调处理器
     */
    public void runWorkflowAsync(CozeWorkflowEnum workflowEnum, Map<String, Object> parameters, 
                                WorkflowCallback callback) {
        runWorkflowAsync(workflowEnum.getWorkflowId(), parameters, callback);
    }

    /**
     * 异步运行工作流
     *
     * @param workflowId 工作流ID
     * @param parameters 工作流参数
     * @param callback   回调处理器
     */
    public void runWorkflowAsync(String workflowId, Map<String, Object> parameters, 
                                WorkflowCallback callback) {
        try {
            // 参数校验
            if (!StringUtils.hasText(workflowId)) {
                callback.onError(new IllegalArgumentException("工作流ID不能为空"));
                return;
            }

            if (cozeClient == null) {
                callback.onError(new IllegalStateException("Coze客户端未初始化"));
                return;
            }

            // 构建请求参数
            Map<String, Object> requestParams = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
            
            RunWorkflowReq request = RunWorkflowReq.builder()
                    .workflowID(workflowId)
                    .parameters(requestParams)
                    .build();

            // 异步执行工作流
            StringBuilder resultBuilder = new StringBuilder();
            Flowable<WorkflowEvent> flowable = cozeClient.workflows().runs().stream(request);
            
            flowable.subscribe(
                event -> {
                    try {
                        handleWorkflowEvent(event, resultBuilder);
                        callback.onEvent(event);
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                },
                callback::onError,
                () -> {
                    String result = resultBuilder.toString();
                    callback.onComplete(result);
                    log.info("异步工作流执行完成: {}", workflowId);
                }
            );

        } catch (Exception e) {
            log.error("异步工作流执行失败: workflowId={}, parameters={}", workflowId, parameters, e);
            callback.onError(e);
        }
    }

    /**
     * 处理工作流事件
     *
     * @param event         工作流事件
     * @param resultBuilder 结果构建器
     */
    private void handleWorkflowEvent(WorkflowEvent event, StringBuilder resultBuilder) {
        WorkflowEventType eventType = event.getEvent();
        if (eventType != null) {
            if (eventType.equals(MESSAGE)) {
                if (event.getMessage() != null && StringUtils.hasText(event.getMessage().getContent())) {
                    resultBuilder.append(event.getMessage().getContent());
                }
            } else if (eventType.equals(ERROR)) {
                if (event.getError() != null) {
                    log.warn("工作流执行出现错误: {}", event.getError());
                    resultBuilder.append("[ERROR]: ").append(event.getError());
                }
            } else if (eventType.equals(INTERRUPT)) {
                log.info("工作流执行被中断: {}", event.getInterrupt());
            } else {
                log.debug("收到工作流事件: {}", eventType);
            }
        }
    }

    /**
     * 生成缓存键
     *
     * @param workflowId 工作流ID
     * @param parameters 参数
     * @return 缓存键
     */
    private String generateCacheKey(String workflowId, Map<String, Object> parameters) {
        String paramHash = parameters != null ? String.valueOf(parameters.hashCode()) : "empty";
        return cozeConfig.getCache().getKeyPrefix() + "workflow:" + workflowId + ":" + paramHash;
    }

    /**
     * 清除工作流缓存
     *
     * @param workflowId 工作流ID
     */
    public void clearWorkflowCache(String workflowId) {
        try {
            String pattern = cozeConfig.getCache().getKeyPrefix() + "workflow:" + workflowId + ":*";
            // 这里可以根据需要实现批量删除缓存的逻辑
            log.info("清除工作流缓存: {}", workflowId);
        } catch (Exception e) {
            log.error("清除工作流缓存失败: {}", workflowId, e);
        }
    }

    /**
     * 检查Coze客户端是否可用
     *
     * @return 是否可用
     */
    public boolean isClientAvailable() {
        return cozeClient != null && StringUtils.hasText(cozeConfig.getAuth().getApiKey());
    }

    /**
     * 工作流回调接口
     */
    public interface WorkflowCallback {
        /**
         * 事件处理
         *
         * @param event 工作流事件
         */
        void onEvent(WorkflowEvent event);

        /**
         * 执行完成
         *
         * @param result 最终结果
         */
        void onComplete(String result);

        /**
         * 执行出错
         *
         * @param error 错误信息
         */
        void onError(Throwable error);
    }
}
