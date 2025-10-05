package com.xiaou.sensitive.engine;

import cn.hutool.core.util.StrUtil;
import com.xiaou.sensitive.domain.SensitiveHomophone;
import com.xiaou.sensitive.domain.SensitiveSimilarChar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 文本预处理器
 * 用于处理变形词检测前的文本规范化
 *
 * @author xiaou
 */
@Slf4j
@Component
public class TextPreprocessor {

    /**
     * 特殊字符正则表达式
     */
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{}|;:'\",.<>?/~`\\s\\u200B-\\u200D\\uFEFF]");

    /**
     * 全角半角映射
     */
    private static final Map<Character, Character> FULL_TO_HALF_MAP = new HashMap<>();

    static {
        // 全角数字和字母转半角
        for (int i = 0xFF10; i <= 0xFF19; i++) { // 全角数字 0-9
            FULL_TO_HALF_MAP.put((char) i, (char) (i - 0xFEE0));
        }
        for (int i = 0xFF21; i <= 0xFF3A; i++) { // 全角大写字母 A-Z
            FULL_TO_HALF_MAP.put((char) i, (char) (i - 0xFEE0));
        }
        for (int i = 0xFF41; i <= 0xFF5A; i++) { // 全角小写字母 a-z
            FULL_TO_HALF_MAP.put((char) i, (char) (i - 0xFEE0));
        }
        // 全角空格
        FULL_TO_HALF_MAP.put('\u3000', ' ');
    }

    /**
     * 同音字映射（运行时加载）
     */
    private Map<Character, String> homophoneMap = new HashMap<>();

    /**
     * 形似字映射（运行时加载）
     */
    private Map<Character, String> similarCharMap = new HashMap<>();

    /**
     * 文本预处理
     *
     * @param text 原始文本
     * @param removeSpecialChars 是否移除特殊字符
     * @param convertFullWidth 是否转换全角
     * @param replaceHomophone 是否替换同音字
     * @param replaceSimilarChar 是否替换形似字
     * @return 处理后的文本
     */
    public String preprocess(String text,
                            boolean removeSpecialChars,
                            boolean convertFullWidth,
                            boolean replaceHomophone,
                            boolean replaceSimilarChar) {
        if (StrUtil.isBlank(text)) {
            return text;
        }

        String result = text;

        // 1. 全角转半角
        if (convertFullWidth) {
            result = convertFullWidthToHalfWidth(result);
        }

        // 2. 移除特殊字符
        if (removeSpecialChars) {
            result = removeSpecialChars(result);
        }

        // 3. 转小写
        result = result.toLowerCase();

        // 4. 同音字替换
        if (replaceHomophone && !homophoneMap.isEmpty()) {
            result = replaceWithHomophone(result);
        }

        // 5. 形似字替换
        if (replaceSimilarChar && !similarCharMap.isEmpty()) {
            result = replaceWithSimilarChar(result);
        }

        return result;
    }

    /**
     * 全角转半角
     */
    private String convertFullWidthToHalfWidth(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            sb.append(FULL_TO_HALF_MAP.getOrDefault(c, c));
        }
        return sb.toString();
    }

    /**
     * 移除特殊字符
     */
    private String removeSpecialChars(String text) {
        return SPECIAL_CHAR_PATTERN.matcher(text).replaceAll("");
    }

    /**
     * 同音字替换
     */
    private String replaceWithHomophone(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (homophoneMap.containsKey(c)) {
                // 使用第一个同音字替换
                String homophones = homophoneMap.get(c);
                String[] chars = homophones.split(",");
                sb.append(chars.length > 0 ? chars[0] : c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 形似字替换
     */
    private String replaceWithSimilarChar(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            if (similarCharMap.containsKey(c)) {
                // 使用第一个形似字替换
                String similars = similarCharMap.get(c);
                String[] chars = similars.split(",");
                sb.append(chars.length > 0 ? chars[0] : c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 加载同音字映射
     */
    public void loadHomophoneMap(List<SensitiveHomophone> homophones) {
        if (homophones == null || homophones.isEmpty()) {
            return;
        }

        Map<Character, String> newMap = new HashMap<>();
        for (SensitiveHomophone homophone : homophones) {
            if (StrUtil.isNotBlank(homophone.getOriginalChar()) 
                && StrUtil.isNotBlank(homophone.getHomophoneChars())) {
                char originalChar = homophone.getOriginalChar().charAt(0);
                newMap.put(originalChar, homophone.getHomophoneChars());
            }
        }
        this.homophoneMap = newMap;
        log.info("加载同音字映射完成，数量: {}", newMap.size());
    }

    /**
     * 加载形似字映射
     */
    public void loadSimilarCharMap(List<SensitiveSimilarChar> similarChars) {
        if (similarChars == null || similarChars.isEmpty()) {
            return;
        }

        Map<Character, String> newMap = new HashMap<>();
        for (SensitiveSimilarChar similarChar : similarChars) {
            if (StrUtil.isNotBlank(similarChar.getOriginalChar()) 
                && StrUtil.isNotBlank(similarChar.getSimilarChars())) {
                char originalChar = similarChar.getOriginalChar().charAt(0);
                newMap.put(originalChar, similarChar.getSimilarChars());
            }
        }
        this.similarCharMap = newMap;
        log.info("加载形似字映射完成，数量: {}", newMap.size());
    }

    /**
     * 刷新所有映射
     */
    public void refreshMappings(List<SensitiveHomophone> homophones, 
                               List<SensitiveSimilarChar> similarChars) {
        loadHomophoneMap(homophones);
        loadSimilarCharMap(similarChars);
    }
}
