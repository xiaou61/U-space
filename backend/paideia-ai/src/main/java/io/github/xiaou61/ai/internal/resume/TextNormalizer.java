package io.github.xiaou61.ai.internal.resume;

import java.text.Normalizer;
import java.util.Locale;

/**
 * 文本归一化（WORK-018）。
 *
 * <p><b>三个地方必须用同一套归一化</b>：解析阶段的引证校验、关键词三桶分类、事实闸门的
 * 数字与重合度比对。各写一套的后果是"同一份简历，引证能对上、三桶却说没有"——
 * 那种不一致极难查，因为它两个方向都不报错。
 *
 * <p>四步，顺序有意义：
 * <ol>
 *   <li>{@code NFKC}：全角字母数字变半角（中文简历里"Ｊａｖａ"常见），圈号、连字也一并对齐。</li>
 *   <li>小写：`Java` / `JAVA` / `java` 是同一件事。</li>
 *   <li>空白折叠：换行与多空格在比对时都只是一个分隔符。</li>
 *   <li>去首尾空白。</li>
 * </ol>
 *
 * <p><b>刻意不做的</b>：不去标点、不去停用词。去标点会让 `ci/cd` 与 `cicd` 之外还产生
 * `cicd`→`cicd` 之外的误配（例如 `c++` 变成 `c`），而 `c++` 与 `c` 是两个完全不同的技能。
 */
public final class TextNormalizer {

    private TextNormalizer() {
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }
}
