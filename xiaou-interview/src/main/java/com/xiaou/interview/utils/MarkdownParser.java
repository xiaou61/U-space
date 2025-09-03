package com.xiaou.interview.utils;

import cn.hutool.core.util.StrUtil;
import com.xiaou.interview.domain.InterviewQuestion;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown解析工具类
 *
 * @author xiaou
 */
@Slf4j
public class MarkdownParser {

    /**
     * 题目分割正则：以 ## 开头的行
     */
    private static final Pattern QUESTION_SPLIT_PATTERN = Pattern.compile("^## (.+)$", Pattern.MULTILINE);

    /**
     * 解析结果
     */
    @Data
    public static class ParseResult {
        /**
         * 解析成功的题目列表
         */
        private List<InterviewQuestion> questions;

        /**
         * 解析错误信息
         */
        private List<String> errors;

        /**
         * 解析成功数量
         */
        private int successCount;

        /**
         * 解析失败数量
         */
        private int failureCount;

        public ParseResult() {
            this.questions = new ArrayList<>();
            this.errors = new ArrayList<>();
            this.successCount = 0;
            this.failureCount = 0;
        }
    }

    /**
     * 解析Markdown内容
     *
     * @param markdownContent MD内容
     * @param questionSetId   题单ID
     * @return 解析结果
     */
    public static ParseResult parseMarkdown(String markdownContent, Long questionSetId) {
        ParseResult result = new ParseResult();

        if (StrUtil.isBlank(markdownContent)) {
            result.getErrors().add("Markdown内容不能为空");
            return result;
        }

        try {
            // 按 ## 分割题目
            String[] sections = splitByQuestionHeaders(markdownContent);
            
            int sortOrder = 1;
            for (String section : sections) {
                if (StrUtil.isBlank(section)) {
                    continue;
                }

                try {
                    InterviewQuestion question = parseQuestionSection(section, questionSetId, sortOrder);
                    if (question != null) {
                        result.getQuestions().add(question);
                        result.setSuccessCount(result.getSuccessCount() + 1);
                        sortOrder++;
                    }
                } catch (Exception e) {
                    log.warn("解析题目失败: {}", e.getMessage());
                    result.getErrors().add("第" + sortOrder + "题解析失败: " + e.getMessage());
                    result.setFailureCount(result.getFailureCount() + 1);
                }
            }

        } catch (Exception e) {
            log.error("解析Markdown失败", e);
            result.getErrors().add("解析失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 按题目标题分割内容
     */
    private static String[] splitByQuestionHeaders(String content) {
        // 使用正则表达式找到所有 ## 开头的位置
        Matcher matcher = QUESTION_SPLIT_PATTERN.matcher(content);
        List<Integer> positions = new ArrayList<>();
        
        while (matcher.find()) {
            positions.add(matcher.start());
        }

        if (positions.isEmpty()) {
            throw new RuntimeException("未找到题目标题（以 ## 开头的行）");
        }

        List<String> sections = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            int start = positions.get(i);
            int end = (i + 1 < positions.size()) ? positions.get(i + 1) : content.length();
            sections.add(content.substring(start, end).trim());
        }

        return sections.toArray(new String[0]);
    }

    /**
     * 解析单个题目段落
     */
    private static InterviewQuestion parseQuestionSection(String section, Long questionSetId, int sortOrder) {
        if (StrUtil.isBlank(section)) {
            return null;
        }

        // 提取题目标题
        Matcher titleMatcher = QUESTION_SPLIT_PATTERN.matcher(section);
        if (!titleMatcher.find()) {
            throw new RuntimeException("无法提取题目标题");
        }

        String title = titleMatcher.group(1).trim();
        if (StrUtil.isBlank(title)) {
            throw new RuntimeException("题目标题不能为空");
        }

        // 获取标题后的内容作为答案
        String content = section.substring(titleMatcher.end()).trim();
        
        // 验证答案内容
        if (StrUtil.isBlank(content)) {
            throw new RuntimeException("答案内容不能为空");
        }

        // 创建题目对象，答案是标题后的所有内容
        InterviewQuestion question = new InterviewQuestion()
                .setQuestionSetId(questionSetId)
                .setTitle(title)
                .setAnswer(content)   // 答案是标题后的所有内容
                .setSortOrder(sortOrder)
                .setViewCount(0)
                .setFavoriteCount(0);

        return question;
    }

    /**
     * 预览解析结果（不包含题单ID）
     */
    public static ParseResult previewParse(String markdownContent) {
        return parseMarkdown(markdownContent, null);
    }

    /**
     * 验证Markdown格式
     */
    public static List<String> validateMarkdown(String markdownContent) {
        List<String> errors = new ArrayList<>();

        if (StrUtil.isBlank(markdownContent)) {
            errors.add("Markdown内容不能为空");
            return errors;
        }

        // 检查是否包含题目标题
        Matcher matcher = QUESTION_SPLIT_PATTERN.matcher(markdownContent);
        if (!matcher.find()) {
            errors.add("未找到有效的题目标题（需要以 ## 开头）");
        }

        // 检查题目数量
        int questionCount = 0;
        matcher.reset();
        while (matcher.find()) {
            questionCount++;
        }

        if (questionCount == 0) {
            errors.add("至少需要包含一个题目");
        } else if (questionCount > 200) {
            errors.add("单次导入题目数量不能超过200个");
        }

        return errors;
    }
} 