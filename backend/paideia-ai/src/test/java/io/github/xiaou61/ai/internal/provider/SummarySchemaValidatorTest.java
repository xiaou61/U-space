package io.github.xiaou61.ai.internal.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xiaou61.ai.SummaryCard;
import io.github.xiaou61.platform.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 卡片字段校验。
 *
 * <p>这一组用例里最重要的是**空内容**那条：DeepSeek 的思考 token 吃满 `max_tokens` 时
 * 静默返回空，如果它被判成"生成成功"，空卡片会被写进缓存并且永远命中缓存、永不自愈。
 */
class SummarySchemaValidatorTest {

    private final SummarySchemaValidator validator = new SummarySchemaValidator(new ObjectMapper());

    @Test
    @DisplayName("正常卡片解析成结构化字段")
    void parsesValidCard() {
        String raw = """
                {"headline":"三次握手是为了双向确认收发能力",
                 "sections":[{"heading":"为什么是三次","body":"两次无法确认客户端已收到 SYN-ACK。"}]}
                """;
        SummaryCard card = validator.validate(raw);
        assertThat(card.headline()).isEqualTo("三次握手是为了双向确认收发能力");
        assertThat(card.sections()).hasSize(1);
        assertThat(card.sections().get(0).heading()).isEqualTo("为什么是三次");
        assertThat(card.isEmpty()).isFalse();
    }

    @Test
    @DisplayName("模型套了 ```json 代码块也能剥掉")
    void stripsCodeFence() {
        String raw = "```json\n{\"headline\":\"标题\",\"sections\":[{\"heading\":\"小节\",\"body\":\"正文\"}]}\n```";
        assertThat(validator.validate(raw).headline()).isEqualTo("标题");
    }

    @Test
    @DisplayName("空内容按失败处理（思考 token 吃满 max_tokens 时会这样）")
    void rejectsEmptyOutput() {
        assertThatThrownBy(() -> validator.validate(""))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("模型没有返回可用的精简结果");
        assertThatThrownBy(() -> validator.validate("   "))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("缺字段、空小节、非 JSON 都按失败处理")
    void rejectsMalformedCards() {
        assertThatThrownBy(() -> validator.validate("这不是 JSON")).isInstanceOf(BizException.class);
        assertThatThrownBy(() -> validator.validate("{\"sections\":[{\"heading\":\"a\",\"body\":\"b\"}]}"))
                .isInstanceOf(BizException.class) // 缺 headline
                .hasMessageContaining("headline");
        assertThatThrownBy(() -> validator.validate("{\"headline\":\"标题\",\"sections\":[]}"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("小节");
        assertThatThrownBy(() -> validator.validate(
                "{\"headline\":\"标题\",\"sections\":[{\"heading\":\"小节\",\"body\":\"  \"}]}"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("正文为空");
    }

    @Test
    @DisplayName("异常信息里不带原始模型输出")
    void doesNotEchoRawOutput() {
        String raw = "{\"headline\":\"\",\"sections\":[{\"heading\":\"x\",\"body\":\"y\"}]}";
        assertThatThrownBy(() -> validator.validate(raw))
                .satisfies(exception -> assertThat(exception.getMessage()).doesNotContain("sections"));
    }
}
