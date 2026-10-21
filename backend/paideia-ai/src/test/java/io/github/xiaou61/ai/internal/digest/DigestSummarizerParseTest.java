package io.github.xiaou61.ai.internal.digest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * AI 输出的解析与降级（WORK-027 TASK-006）。
 *
 * <p>这些用例只喂字符串给 {@code parse}，不连上游——编辑逻辑的正确性不该依赖网络。
 */
class DigestSummarizerParseTest {

    private final DigestSummarizer summarizer = new DigestSummarizer(null);

    private static final List<String> INPUT = List.of("fp-a", "fp-b", "fp-c");

    @Test
    @DisplayName("正常全返回：三条都解析出来")
    void parsesAllItems() {
        String json = """
                {"items":[
                  {"fingerprint":"fp-a","title":"标题A","summary":"摘要A"},
                  {"fingerprint":"fp-b","title":"标题B","summary":"摘要B"},
                  {"fingerprint":"fp-c","title":"标题C","summary":"摘要C"}
                ]}
                """;

        Map<String, DigestSummarizer.Rewritten> result = summarizer.parse(json, INPUT);

        assertThat(result).hasSize(3);
        assertThat(result.get("fp-b").title()).isEqualTo("标题B");
        assertThat(result.get("fp-b").summary()).isEqualTo("摘要B");
    }

    @Test
    @DisplayName("少返回两条：只解析出返回的，缺的由调用方标降级")
    void partialResponseLeavesOthersMissing() {
        String json = """
                {"items":[{"fingerprint":"fp-a","title":"标题A","summary":"摘要A"}]}
                """;

        Map<String, DigestSummarizer.Rewritten> result = summarizer.parse(json, INPUT);

        assertThat(result).containsOnlyKeys("fp-a");
    }

    @Test
    @DisplayName("**返回集合外的指纹被丢弃**——模型编的键不能进库")
    void rejectsUnknownFingerprint() {
        String json = """
                {"items":[
                  {"fingerprint":"fp-a","title":"标题A","summary":"摘要A"},
                  {"fingerprint":"编造的键","title":"垃圾","summary":"垃圾"}
                ]}
                """;

        Map<String, DigestSummarizer.Rewritten> result = summarizer.parse(json, INPUT);

        assertThat(result).containsOnlyKeys("fp-a");
    }

    @Test
    @DisplayName("空内容按失败处理（思考 token 吃满的正常表现）")
    void emptyResponseIsFailure() {
        assertThat(summarizer.parse("", INPUT)).isEmpty();
        assertThat(summarizer.parse("   ", INPUT)).isEmpty();
    }

    @Test
    @DisplayName("没有 JSON 对象时返回空，不抛异常")
    void noJsonReturnsEmpty() {
        assertThat(summarizer.parse("抱歉，我无法完成这个任务。", INPUT)).isEmpty();
    }

    @Test
    @DisplayName("被 markdown 围栏包住也能解析")
    void toleratesMarkdownFence() {
        String fenced = """
                ```json
                {"items":[{"fingerprint":"fp-a","title":"标题A","summary":"摘要A"}]}
                ```
                """;

        assertThat(summarizer.parse(fenced, INPUT)).containsOnlyKeys("fp-a");
    }

    @Test
    @DisplayName("JSON 坏掉返回空，不抛异常")
    void malformedJsonReturnsEmpty() {
        assertThat(summarizer.parse("{\"items\":[{\"fingerprint\":", INPUT)).isEmpty();
    }

    @Test
    @DisplayName("标题与摘要都空的那条跳过（不写空行）")
    void skipsBlankEntry() {
        String json = """
                {"items":[
                  {"fingerprint":"fp-a","title":"","summary":""},
                  {"fingerprint":"fp-b","title":"标题B","summary":"摘要B"}
                ]}
                """;

        assertThat(summarizer.parse(json, INPUT)).containsOnlyKeys("fp-b");
    }
}
