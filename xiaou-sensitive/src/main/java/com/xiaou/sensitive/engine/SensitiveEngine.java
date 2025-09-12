package com.xiaou.sensitive.engine;

import java.util.List;
import java.util.Set;

/**
 * 敏感词检测引擎接口
 */
public interface SensitiveEngine {

    /**
     * 初始化引擎，加载敏感词库
     * @param sensitiveWords 敏感词集合
     */
    void initialize(Set<String> sensitiveWords);

    /**
     * 检测文本中的敏感词
     * @param text 待检测文本
     * @return 命中的敏感词列表
     */
    List<String> findSensitiveWords(String text);

    /**
     * 替换文本中的敏感词
     * @param text 待处理文本
     * @param replacement 替换字符，默认为 ***
     * @return 处理后的文本
     */
    String replaceSensitiveWords(String text, String replacement);

    /**
     * 检测文本是否包含敏感词
     * @param text 待检测文本
     * @return true-包含敏感词，false-不包含
     */
    boolean containsSensitiveWords(String text);

    /**
     * 刷新敏感词库
     * @param sensitiveWords 新的敏感词集合
     */
    void refresh(Set<String> sensitiveWords);
} 