package io.github.xiaou61.ai.internal.web;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xiaou61.ai.RunOutcome;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 一轮的终态归类。
 *
 * <p>这三条分不开，`ai_runs` 就只能回答"失败了多少"，回答不了"多少是被用户放弃的"——
 * 而用户主动停止恰恰是最诚实的"这次回答不行"信号，混进 failed 就丢了。
 *
 * <p>用纯函数测而不是起 SSE：真实路径上这两个信号都是**异常套异常**进来的
 * （上游失败会把下层异常包一层），所以要测的正是"沿 cause 链找得到"。
 */
class AssistantConversationOutcomeTest {

    /** `send()` 在连接已断时抛的形状。 */
    private static BizException clientGone() {
        return new AssistantConversationController.ClientGoneException();
    }

    /** 上游超时：Reactor 的 `.timeout()` 抛 TimeoutException，被包成"上游不可用"再上报。 */
    private static BizException upstreamTimeout() {
        return new BizException(ErrorCode.INTERNAL, "上游模型服务暂时不可用，请稍后再试",
                new TimeoutException("Did not observe any item within the timeout"));
    }

    @Test
    @DisplayName("写事件时连接已断 → cancelled，不是 failed")
    void clientGoneIsCancelled() {
        assertThat(AssistantConversationController.outcomeOf(clientGone())).isEqualTo(RunOutcome.CANCELLED);
        assertThat(AssistantConversationController.errorTypeOf(clientGone(), RunOutcome.CANCELLED))
                .isEqualTo("client_gone");
    }

    @Test
    @DisplayName("超时被包在 cause 链里也能认出来 → timeout")
    void wrappedTimeoutIsTimeout() {
        assertThat(AssistantConversationController.outcomeOf(upstreamTimeout())).isEqualTo(RunOutcome.TIMEOUT);
        assertThat(AssistantConversationController.errorTypeOf(upstreamTimeout(), RunOutcome.TIMEOUT))
                .isEqualTo("upstream_timeout");
    }

    @Test
    @DisplayName("其余失败仍是 failed，error_type 取错误码")
    void otherFailuresStayFailed() {
        BizException other = new BizException(ErrorCode.INVALID_ARGUMENT, "上游拒绝了这组凭据");
        assertThat(AssistantConversationController.outcomeOf(other)).isEqualTo(RunOutcome.FAILED);
        assertThat(AssistantConversationController.errorTypeOf(other, RunOutcome.FAILED))
                .isEqualTo("INVALID_ARGUMENT");
    }

    @Test
    @DisplayName("用户消息里出现「连接已断开」这几个字不算取消")
    void messageSubstringAloneDoesNotCancel() {
        // 判据是**异常类型**，不是消息内容：上游返回的错误文本完全可能包含这句话
        // （它会被包进 cause 链），按字符串认就把上游故障记成了用户放弃。
        BizException upstream = new BizException(ErrorCode.INTERNAL, "上游模型服务暂时不可用，请稍后再试",
                new IllegalStateException(AssistantConversationController.CLIENT_GONE));
        assertThat(AssistantConversationController.outcomeOf(upstream)).isEqualTo(RunOutcome.FAILED);
    }
}
