package io.github.xiaou61.ai.internal.camp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 题目 JSON 解析（WORK-029 TASK-008）。
 *
 * <p>核心是**空返回必须失败**这一条：把它改成 {@code return List.of()} 会让调用方以为
 * "模型给了 0 道题"是成功，于是建出一个没有题的营、还照常扣费。
 */
class CampItemParserTest {

    private final CampItemParser parser = new CampItemParser();

    private static final String GOOD = """
            {"items":[{"topic":"集合","concept":"HashMap","difficulty":"medium",
            "stem":"HashMap 在并发扩容时可能发生什么？",
            "options":["死循环","内存泄漏","数据丢失","编译失败"],"answerIndex":0,
            "explanation":"死循环是 JDK7 头插法在并发扩容时的典型后果。"}]}
            """;

    @Test
    @DisplayName("正常 JSON 解析出正确条数")
    void parsesGoodJson() {
        List<ItemValidator.RawItem> items = parser.parse(GOOD);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).topic()).isEqualTo("集合");
        assertThat(items.get(0).options()).hasSize(4);
        assertThat(items.get(0).answerIndex()).isZero();
    }

    @Test
    @DisplayName("空内容与全空白都按失败处理（不能当成 0 道题）")
    void emptyResponseIsFailure() {
        assertThatThrownBy(() -> parser.parse(""))
                .isInstanceOf(CampGenerationException.class)
                .extracting(failure -> ((CampGenerationException) failure).kind())
                .isEqualTo(CampGenerationException.Kind.EMPTY_RESPONSE);
        assertThatThrownBy(() -> parser.parse("   \n\t "))
                .isInstanceOf(CampGenerationException.class)
                .extracting(failure -> ((CampGenerationException) failure).kind())
                .isEqualTo(CampGenerationException.Kind.EMPTY_RESPONSE);
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(CampGenerationException.class);
    }

    @Test
    @DisplayName("坏 JSON 抛 BAD_SHAPE")
    void badJsonIsFailure() {
        assertThatThrownBy(() -> parser.parse("{不是 JSON"))
                .isInstanceOf(CampGenerationException.class)
                .extracting(failure -> ((CampGenerationException) failure).kind())
                .isEqualTo(CampGenerationException.Kind.BAD_SHAPE);
    }

    @Test
    @DisplayName("options 是字符串而不是数组时抛错")
    void optionsMustBeArray() {
        String bad = """
                {"items":[{"topic":"集合","concept":"","difficulty":"easy","stem":"题干够长了",
                "options":"死循环","answerIndex":0,"explanation":"解析在这里写着。"}]}
                """;
        assertThatThrownBy(() -> parser.parse(bad))
                .isInstanceOf(CampGenerationException.class)
                .extracting(failure -> ((CampGenerationException) failure).kind())
                .isEqualTo(CampGenerationException.Kind.BAD_SHAPE);
    }

    @Test
    @DisplayName("没有 items 数组时抛错")
    void missingItemsIsFailure() {
        assertThatThrownBy(() -> parser.parse("{\"questions\":[]}"))
                .isInstanceOf(CampGenerationException.class);
    }

    @Test
    @DisplayName("markdown 围栏能被剥掉（模型常无视提示词加围栏）")
    void stripsCodeFence() {
        String fenced = "```json\n" + GOOD + "\n```";
        assertThat(parser.parse(fenced)).hasSize(1);
    }

    @Test
    @DisplayName("answerIndex 是字符串时抛错（不做隐式转换）")
    void answerIndexMustBeInteger() {
        String bad = """
                {"items":[{"topic":"集合","concept":"","difficulty":"easy","stem":"题干够长了",
                "options":["A项","B项","C项","D项"],"answerIndex":"0",
                "explanation":"解析在这里写着。"}]}
                """;
        assertThatThrownBy(() -> parser.parse(bad))
                .isInstanceOf(CampGenerationException.class);
    }
}
