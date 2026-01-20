package com.xiaou.ai.dto.interview;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * AI面试总结结果
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class InterviewSummaryResult {

    /**
     * 面试总结内容
     */
    private String summary;

    /**
     * 整体评级：优秀/良好/一般/需加强
     */
    private String overallLevel;

    /**
     * 改进建议列表
     */
    private List<String> suggestions;

    /**
     * 是否为降级结果
     */
    private boolean fallback;

    /**
     * 创建降级结果
     */
    public static InterviewSummaryResult fallbackResult(int totalScore) {
        InterviewSummaryResult result = new InterviewSummaryResult();

        // 根据分数生成评级
        String level;
        String summary;
        if (totalScore >= 80) {
            level = "优秀";
            summary = "您在本次面试中表现优秀，对技术知识掌握扎实，回答清晰有条理。";
        } else if (totalScore >= 60) {
            level = "良好";
            summary = "您在本次面试中表现良好，基础知识掌握较好，部分知识点可以继续深入。";
        } else if (totalScore >= 40) {
            level = "一般";
            summary = "您在本次面试中表现一般，建议加强基础知识的学习和理解。";
        } else {
            level = "需加强";
            summary = "您在本次面试中还有较大的提升空间，建议系统学习相关技术知识。";
        }

        result.setSummary(summary);
        result.setOverallLevel(level);
        result.setSuggestions(List.of(
                "建议复习核心概念和原理",
                "多进行实际编码练习",
                "关注技术社区和最新动态"
        ));
        result.setFallback(true);

        return result;
    }
}
