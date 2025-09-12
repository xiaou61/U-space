package com.xiaou.sensitive.api;

import com.xiaou.sensitive.api.dto.SensitiveCheckRequest;
import com.xiaou.sensitive.api.dto.SensitiveCheckResponse;

import java.util.List;

/**
 * 敏感词检测服务接口
 */
public interface SensitiveCheckService {

    /**
     * 检测文本中的敏感词
     * @param request 检测请求
     * @return 检测结果
     */
    SensitiveCheckResponse checkText(SensitiveCheckRequest request);

    /**
     * 批量检测文本中的敏感词
     * @param requests 检测请求列表
     * @return 检测结果列表
     */
    List<SensitiveCheckResponse> checkTextBatch(List<SensitiveCheckRequest> requests);

    /**
     * 检测文本是否包含敏感词（简化版）
     * @param text 待检测文本
     * @param module 模块名称
     * @return 是否包含敏感词
     */
    boolean containsSensitiveWords(String text, String module);

    /**
     * 替换文本中的敏感词（简化版）
     * @param text 待处理文本
     * @param module 模块名称
     * @return 处理后的文本
     */
    String replaceSensitiveWords(String text, String module);

    /**
     * 检测文本是否允许发布（简化版）
     * @param text 待检测文本
     * @param module 模块名称
     * @param businessId 业务ID
     * @param userId 用户ID
     * @return 是否允许发布
     */
    boolean isAllowed(String text, String module, Long businessId, Long userId);

    /**
     * 刷新敏感词库
     */
    void refreshWordLibrary();
} 