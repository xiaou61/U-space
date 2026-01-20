package com.xiaou.ai.dto.sql;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL分析结果
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class SqlAnalyzeResult {

    /**
     * 性能评分（0-100）
     */
    private Integer score;

    /**
     * 识别出的问题列表
     */
    private List<Problem> problems;

    /**
     * EXPLAIN逐行解析
     */
    private List<ExplainAnalysis> explainAnalysis;

    /**
     * 优化建议列表
     */
    private List<Suggestion> suggestions;

    /**
     * 优化后的SQL
     */
    private String optimizedSql;

    /**
     * 相关知识点
     */
    private List<String> knowledgePoints;

    /**
     * 是否为降级结果
     */
    private boolean fallback;

    /**
     * 性能问题
     */
    @Data
    @Accessors(chain = true)
    public static class Problem {
        /**
         * 问题类型枚举
         */
        private String type;

        /**
         * 严重程度：HIGH/MEDIUM/LOW
         */
        private String severity;

        /**
         * 问题描述
         */
        private String description;

        /**
         * 问题位置
         */
        private String location;
    }

    /**
     * EXPLAIN解析
     */
    @Data
    @Accessors(chain = true)
    public static class ExplainAnalysis {
        private String table;
        private String type;
        private String typeExplain;
        private String key;
        private String keyExplain;
        private Integer rows;
        private String extra;
        private String extraExplain;
        private String issue;
    }

    /**
     * 优化建议
     */
    @Data
    @Accessors(chain = true)
    public static class Suggestion {
        /**
         * 建议类型：ADD_INDEX/MODIFY_INDEX/REWRITE_SQL/SPLIT_QUERY/OTHER
         */
        private String type;

        /**
         * 建议标题
         */
        private String title;

        /**
         * DDL语句（索引建议时）
         */
        private String ddl;

        /**
         * 优化后SQL（SQL重写时）
         */
        private String sql;

        /**
         * 建议原因
         */
        private String reason;

        /**
         * 预期提升效果
         */
        private String expectedImprovement;
    }

    /**
     * 创建降级结果（基础规则检测）
     */
    public static SqlAnalyzeResult fallbackResult(String explainResult) {
        SqlAnalyzeResult result = new SqlAnalyzeResult();
        List<Problem> problems = new ArrayList<>();

        // 简单规则检测
        int score = 70;
        
        if (explainResult != null) {
            // 检测全表扫描
            if (explainResult.contains("ALL") && !explainResult.contains("index")) {
                problems.add(new Problem()
                        .setType("FULL_TABLE_SCAN")
                        .setSeverity("HIGH")
                        .setDescription("检测到可能存在全表扫描")
                        .setLocation("EXPLAIN结果"));
                score -= 30;
            }

            // 检测filesort
            if (explainResult.contains("filesort")) {
                problems.add(new Problem()
                        .setType("FILE_SORT")
                        .setSeverity("MEDIUM")
                        .setDescription("检测到文件排序，可能影响性能")
                        .setLocation("Extra字段"));
                score -= 15;
            }

            // 检测temporary
            if (explainResult.contains("temporary")) {
                problems.add(new Problem()
                        .setType("TEMP_TABLE")
                        .setSeverity("MEDIUM")
                        .setDescription("检测到使用临时表")
                        .setLocation("Extra字段"));
                score -= 15;
            }
        }

        result.setScore(Math.max(0, score));
        result.setProblems(problems);
        result.setExplainAnalysis(List.of());
        result.setSuggestions(List.of(new Suggestion()
                .setType("OTHER")
                .setTitle("建议进行详细分析")
                .setReason("当前为降级模式，建议稍后重试以获取完整AI分析")
                .setExpectedImprovement("获取更详细的优化建议")));
        result.setKnowledgePoints(List.of("索引优化", "EXPLAIN分析"));
        result.setFallback(true);

        return result;
    }
}
