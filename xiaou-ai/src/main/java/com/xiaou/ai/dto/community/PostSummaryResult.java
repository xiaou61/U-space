package com.xiaou.ai.dto.community;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 帖子AI摘要结果
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class PostSummaryResult {

    /**
     * AI生成的摘要
     */
    private String summary;

    /**
     * AI提取的关键词列表
     */
    private List<String> keywords;

    /**
     * 是否为降级结果
     */
    private boolean fallback;

    /**
     * 创建降级结果
     */
    public static PostSummaryResult fallbackResult(String title) {
        return new PostSummaryResult()
                .setSummary("暂无AI摘要")
                .setKeywords(List.of())
                .setFallback(true);
    }
}
