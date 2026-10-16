package io.github.xiaou61.ai.internal.memory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xiaou61.ai.MemoryCandidate;
import io.github.xiaou61.platform.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 记忆整理返回值的规范化（WORK-013 T-04）。
 *
 * <p>这一层的每一条断言都对应一个"模型输出不可信"的具体形态：空内容、非 JSON、缺字段、
 * 编造 id、动作不认识、内容超长。它们**必须整次放弃**而不是尽力而为——写半条记忆的代价比不写高，
 * 因为用户看不到、下次整理又会基于它判重。
 *
 * <p>唯一"不放弃"的情形是 `kind` 越界：那不是错误，按设计落到全文那路。
 */
class MemoryCandidateParserTest {

    private final MemoryCandidateParser parser = new MemoryCandidateParser();

    @Test
    @DisplayName("空内容当成故障，不是「没什么可记的」")
    void emptyOutputIsFailure() {
        // 这是本项目已知的真实故障模式：DeepSeek 把 max_tokens 吃在思考上时会静默返回空。
        // 若把空当成"没有可记的"，记忆会永远不增长而没有任何报错。
        assertThatThrownBy(() -> parser.parse(""))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("没有返回任何内容");
        assertThatThrownBy(() -> parser.parse("   "))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("空数组是合法结果：这几轮不值得记")
    void emptyCandidatesAreFine() {
        assertThat(parser.parse("{\"candidates\":[]}")).isEmpty();
    }

    @Test
    @DisplayName("非 JSON / 缺 candidates 都判失败")
    void malformedJsonIsFailure() {
        assertThatThrownBy(() -> parser.parse("我觉得可以记一条"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("不是合法 JSON");
        assertThatThrownBy(() -> parser.parse("{\"items\":[]}"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("candidates");
    }

    @Test
    @DisplayName("四种动作各自解析正确，大小写不敏感")
    void allFourActions() {
        String raw = """
                {"candidates":[
                  {"action":"add","store":"text","content":"他喜欢先看例子","reason":"偏好"},
                  {"action":"UPDATE","store":"text","targetId":7,"content":"他现在更想先看原理"},
                  {"action":"delete","store":"structured","targetId":3,"reason":"他后来学会了"},
                  {"action":"skip","store":"text","reason":"寒暄"}
                ]}
                """;
        var candidates = parser.parse(raw);

        assertThat(candidates).hasSize(4);
        assertThat(candidates.get(0).action()).isEqualTo(MemoryCandidate.Action.ADD);
        assertThat(candidates.get(0).store()).isEqualTo(MemoryCandidate.Store.TEXT);
        assertThat(candidates.get(0).content()).isEqualTo("他喜欢先看例子");
        assertThat(candidates.get(1).action()).isEqualTo(MemoryCandidate.Action.UPDATE);
        assertThat(candidates.get(1).targetId()).isEqualTo(7L);
        assertThat(candidates.get(2).action()).isEqualTo(MemoryCandidate.Action.DELETE);
        assertThat(candidates.get(2).targetId()).isEqualTo(3L);
        // SKIP 不校验内容——它本来就不会落库
        assertThat(candidates.get(3).action()).isEqualTo(MemoryCandidate.Action.SKIP);
        assertThat(candidates.get(3).content()).isNull();
    }

    @Test
    @DisplayName("UPDATE / DELETE 没有 targetId 就判失败（不能猜一个 id）")
    void updateAndDeleteNeedTarget() {
        assertThatThrownBy(() -> parser.parse(
                "{\"candidates\":[{\"action\":\"UPDATE\",\"store\":\"text\",\"content\":\"新说法\"}]}"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("targetId");
        assertThatThrownBy(() -> parser.parse(
                "{\"candidates\":[{\"action\":\"DELETE\",\"store\":\"text\"}]}"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("targetId");
    }

    @Test
    @DisplayName("id 只认正整数：0 与负数一定是幻觉")
    void idsMustBePositive() {
        // 负数/0 会被当成"没给" → 判失败；而归属另由 SQL 兜（模型猜到别人的 id 也改不到）
        assertThatThrownBy(() -> parser.parse(
                "{\"candidates\":[{\"action\":\"UPDATE\",\"store\":\"text\",\"targetId\":0,\"content\":\"x\"}]}"))
                .isInstanceOf(BizException.class);
        assertThatThrownBy(() -> parser.parse(
                "{\"candidates\":[{\"action\":\"UPDATE\",\"store\":\"text\",\"targetId\":-3,\"content\":\"x\"}]}"))
                .isInstanceOf(BizException.class);
        assertThatThrownBy(() -> parser.parse(
                "{\"candidates\":[{\"action\":\"UPDATE\",\"store\":\"text\",\"targetId\":\"七\",\"content\":\"x\"}]}"))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("ADD / UPDATE 的内容不能为空、不能超长")
    void contentIsValidated() {
        assertThatThrownBy(() -> parser.parse(
                "{\"candidates\":[{\"action\":\"ADD\",\"store\":\"text\",\"content\":\"  \"}]}"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("内容为空");
        assertThatThrownBy(() -> parser.parse(
                "{\"candidates\":[{\"action\":\"ADD\",\"store\":\"text\",\"content\":\"" + "字".repeat(1025) + "\"}]}"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("超长");
    }

    @Test
    @DisplayName("kind 越界不是错误：那条候选落到全文那路")
    void unknownKindFallsBackToText() {
        // 结构化那路必须有白名单内的 kind；模型发明一个词时，宁可按"他说过的话"记，也不丢这条
        var candidates = parser.parse(
                "{\"candidates\":[{\"action\":\"ADD\",\"store\":\"structured\",\"kind\":\"心情\",\"content\":\"他最近压力大\"}]}");

        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).store()).isEqualTo(MemoryCandidate.Store.TEXT);
        assertThat(candidates.get(0).kind()).isNull();
    }

    @Test
    @DisplayName("kind 在白名单内就留在结构化那路，并带上 refId")
    void allowlistedKindStaysStructured() {
        var candidates = parser.parse(
                "{\"candidates\":[{\"action\":\"ADD\",\"store\":\"structured\",\"kind\":\"weak_topic\","
                        + "\"refId\":12,\"content\":\"这个条目他还不懂\"}]}");

        assertThat(candidates.get(0).store()).isEqualTo(MemoryCandidate.Store.STRUCTURED);
        assertThat(candidates.get(0).kind()).isEqualTo("weak_topic");
        assertThat(candidates.get(0).refId()).isEqualTo(12L);
    }

    @Test
    @DisplayName("外层的 ```json 代码块会被剥掉（模型偶尔仍会包）")
    void fencedJsonIsStripped() {
        var candidates = parser.parse("""
                ```json
                {"candidates":[{"action":"ADD","store":"text","content":"他准备秋招"}]}
                ```
                """);
        assertThat(candidates).hasSize(1);
    }

    @Test
    @DisplayName("一次给太多候选判失败：防「一口气导入」")
    void tooManyCandidatesRejected() {
        StringBuilder builder = new StringBuilder("{\"candidates\":[");
        for (int i = 0; i < 11; i++) {
            builder.append(i == 0 ? "" : ",").append("{\"action\":\"ADD\",\"store\":\"text\",\"content\":\"x\"}");
        }
        builder.append("]}");
        assertThatThrownBy(() -> parser.parse(builder.toString()))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("候选数量超出上限");
    }
}
