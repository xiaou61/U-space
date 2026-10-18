package io.github.xiaou61.ai.internal.conversation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xiaou61.ai.Harness;
import io.github.xiaou61.ai.InterviewEvaluation;
import io.github.xiaou61.platform.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 点评返回值的校验（WORK-010 T-10）。
 *
 * <p>这一层是"评分能落库"的前提：解析失败一律判失败，**绝不写库**——理由与摘要校验器相同
 * （模型会静默返回空），而一份空评分落库之后，"进度与弱项"就永远错在那一次上。
 */
class InterviewEvaluationParserTest {

    private final InterviewEvaluationParser parser = new InterviewEvaluationParser();

    @Test
    @DisplayName("合法评估：总分、维度、优点、改进点都解析出来")
    void parsesValidEvaluation() {
        String raw = """
                {"overall":3,"dimensions":[
                  {"name":"技术准确性","score":3,"comment":"HashMap 的扩容讲清楚了"},
                  {"name":"表达","score":4,"comment":"结构清楚"}],
                 "strengths":["先说结论再展开"],
                 "improvements":["补一下红黑树的旋转条件"]}
                """;

        InterviewEvaluation evaluation = parser.parse(raw);

        assertThat(evaluation.overall()).isEqualTo(3);
        assertThat(evaluation.dimensions()).hasSize(2);
        assertThat(evaluation.dimensions().getFirst().name()).isEqualTo("技术准确性");
        assertThat(evaluation.strengths()).containsExactly("先说结论再展开");
        assertThat(evaluation.improvements()).containsExactly("补一下红黑树的旋转条件");
    }

    @Test
    @DisplayName("空内容按故障处理，不是「没得评」")
    void emptyIsFailure() {
        // DeepSeek 把 max_tokens 吃在思考上时会静默返回空：当成"没得评"会让评分永远不落库
        assertThatThrownBy(() -> parser.parse("")).isInstanceOf(BizException.class)
                .hasMessageContaining("没有返回任何内容");
        assertThatThrownBy(() -> parser.parse("   ")).isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("分数必须是 1–5 的整数：0、6、小数、字符串都拒")
    void scoreMustBeIntegerInRange() {
        // 0 / 6：越界
        assertThatThrownBy(() -> parser.parse(one(0))).isInstanceOf(BizException.class);
        assertThatThrownBy(() -> parser.parse(one(6))).isInstanceOf(BizException.class);
        // 3.5：不是整数
        assertThatThrownBy(() -> parser.parse(one(3.5))).isInstanceOf(BizException.class);
        // "3"：不是数字
        assertThatThrownBy(() -> parser.parse(one("\"3\""))).isInstanceOf(BizException.class);
        // 3.0：可以（有些模型这么写）
        assertThat(parser.parse(one(3.0)).overall()).isEqualTo(3);
    }

    @Test
    @DisplayName("维度为空判失败：一份没有任何维度的评估没有价值")
    void dimensionsRequired() {
        assertThatThrownBy(() -> parser.parse("""
                {"overall":3,"dimensions":[],"strengths":["a"],"improvements":["b"]}
                """))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("维度");
    }

    @Test
    @DisplayName("优点与改进点都为空的评估判失败，不落空壳")
    void emptyAdviceIsFailure() {
        assertThatThrownBy(() -> parser.parse("""
                {"overall":3,"dimensions":[{"name":"技术","score":3,"comment":"还行"}],
                 "strengths":[],"improvements":[]}
                """))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("空的");
    }

    @Test
    @DisplayName("外层的 ```json 代码块会被剥掉")
    void fencedJsonIsStripped() {
        InterviewEvaluation evaluation = parser.parse("""
                ```json
                {"overall":4,"dimensions":[{"name":"技术","score":4,"comment":"好"}],"strengths":["x"],\
                "improvements":["y"]}
                ```
                """);
        assertThat(evaluation.overall()).isEqualTo(4);
    }

    @Test
    @DisplayName("harness 三套齐全，未知值退回通用助手，工具集按 harness 分")
    void harnessRegistryIsComplete() {
        // 三套都有系统提示词，且互不相同——相同就说明注册表写错了（面试会变成闲聊）
        String assistant = HarnessPrompts.system(Harness.ASSISTANT);
        String interview = HarnessPrompts.system(Harness.INTERVIEW);
        String review = HarnessPrompts.system(Harness.REVIEW);
        assertThat(assistant).isNotBlank().isNotEqualTo(interview).isNotEqualTo(review);
        assertThat(interview).isNotEqualTo(review);
        // 版本号各不相同：将来"为什么答得不一样"要能分辨是哪份提示词改过
        assertThat(HarnessPrompts.version(Harness.INTERVIEW)).isNotEqualTo(HarnessPrompts.version(Harness.ASSISTANT));

        // 库里读到脏值不抛错，退回通用助手
        assertThat(Harness.ofOrDefault("nonsense")).isEqualTo(Harness.ASSISTANT);
        assertThat(Harness.ofOrDefault(null)).isEqualTo(Harness.ASSISTANT);
        assertThat(Harness.ofOrDefault("INTERVIEW")).isEqualTo(Harness.INTERVIEW);
    }

    /** 造一份只有 overall 不同的合法评估（其余字段固定合法）。 */
    private static String one(Object overall) {
        return """
                {"overall":%s,"dimensions":[{"name":"技术","score":3,"comment":"还行"}],\
                "strengths":["a"],"improvements":["b"]}
                """.formatted(overall);
    }
}
