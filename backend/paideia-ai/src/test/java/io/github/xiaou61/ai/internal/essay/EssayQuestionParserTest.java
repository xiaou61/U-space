package io.github.xiaou61.ai.internal.essay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xiaou61.platform.BizException;
import org.junit.jupiter.api.Test;

/**
 * 抽题 JSON 的解析与校验（WORK-025）。
 *
 * <p>这里最要紧的是**分清"空数组"与"空内容"**：前者是"这篇帖子没有技术问题"（合法，
 * 不该报警），后者是模型故障（必须抛）。混在一起会让每天的抓取静默失效而没人发现。
 */
class EssayQuestionParserTest {

    private final EssayQuestionParser parser = new EssayQuestionParser();

    @Test
    void parsesValidQuestions() {
        String json = """
                {"questions":[
                  {"title":"HashMap 为什么线程不安全","answer":"因为扩容时并发 rehash 会形成环形链表……（此处省略，测试只关心长度）"},
                  {"title":"JVM 内存模型分几块","answer":"堆、栈、方法区、程序计数器、本地方法栈，各自的生命周期与线程归属不同……"}
                ]}
                """;
        var result = parser.parse(json);
        assertThat(result.questions()).hasSize(2);
        assertThat(result.questions().get(0).title()).isEqualTo("HashMap 为什么线程不安全");
    }

    @Test
    void emptyArrayIsAValidResult() {
        // "这篇帖子没有技术问题"是合法结果，不是故障。
        assertThat(parser.parse("{\"questions\":[]}").questions()).isEmpty();
    }

    @Test
    void blankModelOutputIsAFailure() {
        // 与空数组相反：模型什么都没返回是故障。
        assertThatThrownBy(() -> parser.parse("   ")).isInstanceOf(BizException.class);
        assertThatThrownBy(() -> parser.parse(null)).isInstanceOf(BizException.class);
    }

    @Test
    void nonJsonIsAFailure() {
        assertThatThrownBy(() -> parser.parse("抱歉，我无法处理这个请求"))
                .isInstanceOf(BizException.class);
    }

    @Test
    void stripsMarkdownFence() {
        String json = """
                ```json
                {"questions":[{"title":"Redis 缓存穿透怎么解决","answer":"布隆过滤器或缓存空值，前者省内存但会误判，后者简单但占空间……"}]}
                ```
                """;
        assertThat(parser.parse(json).questions()).hasSize(1);
    }

    @Test
    void dropsMalformedSingleQuestionButKeepsOthers() {
        // 一道题格式不对不该让整篇面经作废——只丢那一条。
        String json = """
                {"questions":[
                  {"title":"","answer":"空标题应当被丢弃，因为题目是条目名"},
                  {"title":"MySQL 索引为什么用 B+ 树","answer":"因为 B+ 树矮胖、叶子节点串成链表适合范围查询，且非叶节点不存数据能放更多键……"},
                  {"title":"答案太短","answer":"略"}
                ]}
                """;
        var result = parser.parse(json);
        assertThat(result.questions()).hasSize(1);
        assertThat(result.questions().get(0).title()).isEqualTo("MySQL 索引为什么用 B+ 树");
    }

    @Test
    void rejectsOverlongTitle() {
        String longTitle = "问".repeat(200);
        String json = "{\"questions\":[{\"title\":\"" + longTitle
                + "\",\"answer\":\"这是一段足够长的解析内容用来通过最小长度校验。\"}]}";
        assertThat(parser.parse(json).questions()).isEmpty();
    }

    @Test
    void missingQuestionsArrayIsAFailure() {
        assertThatThrownBy(() -> parser.parse("{\"data\":[]}")).isInstanceOf(BizException.class);
    }
}
