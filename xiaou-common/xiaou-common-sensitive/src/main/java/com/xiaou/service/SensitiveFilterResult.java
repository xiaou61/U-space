package com.xiaou.service;

import lombok.Data;

import java.util.List;

/**
 * 敏感词过滤结果封装类
 *
 * 用于表示一次敏感词检测与处理的完整结果，
 * 包含是否允许发布、替换后的内容、拦截词和替换词列表等信息。
 */
@Data
public class SensitiveFilterResult {

    /**
     * 是否允许发布内容（true: 允许，false: 拒绝）
     * - 如果命中 1 级敏感词，通常设置为 false（不可发布）
     * - 如果命中 2 级敏感词，可替换后允许发布，设置为 true
     */
    private boolean allowed;

    /**
     * 替换敏感词后的内容（如果 allowed 为 true 时有效）
     */
    private String replacedContent;

    /**
     * 命中的【阻断类】敏感词列表（1 级敏感词）
     * - 如果该字段不为空，表示内容中有不能发布的词
     */
    private List<String> blockedWords;

    /**
     * 命中的【可替换类】敏感词列表（2 级敏感词）
     * - 用于提示哪些词被替换了
     */
    private List<String> replacedWords;

    /**
     * 构造一个【阻断类】敏感词命中结果
     * @param words 命中的敏感词列表
     * @return 过滤结果对象（allowed=false）
     */
    public static SensitiveFilterResult blocked(List<String> words) {
        SensitiveFilterResult r = new SensitiveFilterResult();
        r.allowed = false;
        r.blockedWords = words;
        return r;
    }

    /**
     * 构造一个【可替换类】敏感词命中结果
     * @param replaced 替换后的内容
     * @param replacedWords 被替换的敏感词列表
     * @return 过滤结果对象（allowed=true）
     */
    public static SensitiveFilterResult allowed(String replaced, List<String> replacedWords) {
        SensitiveFilterResult r = new SensitiveFilterResult();
        r.allowed = true;
        r.replacedContent = replaced;
        r.replacedWords = replacedWords;
        return r;
    }
}
