package com.xiaou.mockinterview.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 面试配置响应DTO
 *
 * @author xiaou
 */
@Data
public class InterviewConfigResponse {

    /**
     * 可选难度级别
     */
    private List<LevelOption> levels;

    /**
     * 可选面试类型
     */
    private List<TypeOption> types;

    /**
     * 可选AI风格
     */
    private List<StyleOption> styles;

    /**
     * 可选题目数量
     */
    private List<Integer> questionCounts;

    /**
     * 可选出题模式
     */
    private List<ModeOption> questionModes;

    /**
     * 难度级别选项
     */
    @Data
    public static class LevelOption {
        private Integer code;
        private String name;
        private String description;
    }

    /**
     * 面试类型选项
     */
    @Data
    public static class TypeOption {
        private Integer code;
        private String name;
        private String description;
    }

    /**
     * AI风格选项
     */
    @Data
    public static class StyleOption {
        private Integer code;
        private String name;
        private String description;
    }

    /**
     * 出题模式选项
     */
    @Data
    public static class ModeOption {
        private Integer code;
        private String name;
        private String description;
    }
}
