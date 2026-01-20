package com.xiaou.ai.service.impl;

import cn.hutool.json.JSONObject;
import com.xiaou.ai.dto.community.PostSummaryResult;
import com.xiaou.ai.service.AiCommunityService;
import com.xiaou.ai.util.CozeResponseParser;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.enums.CozeWorkflowEnum;
import com.xiaou.common.utils.CozeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 社区AI服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiCommunityServiceImpl implements AiCommunityService {

    private final CozeUtils cozeUtils;

    @Override
    public PostSummaryResult generatePostSummary(String title, String content) {
        try {
            if (!cozeUtils.isClientAvailable()) {
                log.warn("Coze客户端不可用，使用降级结果");
                return PostSummaryResult.fallbackResult(title);
            }

            // 构建参数
            Map<String, Object> params = new HashMap<>();
            params.put("title", title);
            params.put("content", content);

            // 调用工作流
            Result<String> result = cozeUtils.runWorkflow(
                    CozeWorkflowEnum.COMMUNITY_POST_SUMMARY, params);

            if (!result.isSuccess() || CozeResponseParser.isErrorResponse(result.getData())) {
                log.warn("Coze工作流调用失败: {}", result.getMessage());
                return PostSummaryResult.fallbackResult(title);
            }

            // 解析结果
            JSONObject json = CozeResponseParser.parse(result.getData());
            if (json == null) {
                return PostSummaryResult.fallbackResult(title);
            }

            // 解析摘要
            String summary = CozeResponseParser.getString(json, "summary", "暂无摘要");

            // 解析关键词
            List<String> keywords = parseKeywords(json.get("keywords"));

            return new PostSummaryResult()
                    .setSummary(summary)
                    .setKeywords(keywords)
                    .setFallback(false);

        } catch (Exception e) {
            log.error("生成帖子摘要失败", e);
            return PostSummaryResult.fallbackResult(title);
        }
    }

    /**
     * 解析关键词字段
     */
    private List<String> parseKeywords(Object keywordsObj) {
        if (keywordsObj == null) {
            return List.of();
        }

        List<String> keywords = new ArrayList<>();
        if (keywordsObj instanceof String) {
            // 字符串格式，按逗号分割
            String keywordsStr = ((String) keywordsObj).trim();
            if (!keywordsStr.isEmpty()) {
                for (String kw : keywordsStr.split(",\\s*")) {
                    keywords.add(kw.trim());
                }
            }
        } else if (keywordsObj instanceof List) {
            // 数组格式
            for (Object item : (List<?>) keywordsObj) {
                if (item != null) {
                    keywords.add(item.toString());
                }
            }
        }
        return keywords;
    }
}
