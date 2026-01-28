package com.xiaou.ai.dto.interview;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * AI生成的面试题目
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class GeneratedQuestion {

    /**
     * 问题内容
     */
    private String question;

    /**
     * 参考答案
     */
    private String answer;

    /**
     * 知识点（逗号分隔）
     */
    private String knowledgePoints;
}
