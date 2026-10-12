package io.github.xiaou61.ai;

import java.util.List;

/**
 * 结构化精简卡片：一个总标题 + 若干带小标题的小节（用户裁决的产物形态）。
 *
 * <p>用 record 而不是自由文本：卡片要能**逐字段校验**（缺标题、空小节、超长都按失败处理），
 * 而不是把模型吐出来的一坨字符串照原样渲染。
 */
public record SummaryCard(String headline, List<SummarySection> sections) {

    /** 小节：小标题 + 正文。 */
    public record SummarySection(String heading, String body) {
    }

    public SummaryCard {
        sections = sections == null ? List.of() : List.copyOf(sections);
    }

    /** 供校验与日志使用：卡片是否"有内容"。 */
    public boolean isEmpty() {
        return headline == null || headline.isBlank() || sections.isEmpty();
    }
}
