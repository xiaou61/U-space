package io.github.xiaou61.ai.internal.conversation;

import io.agentscope.core.agent.accumulator.TextAccumulator;
import io.agentscope.core.agent.accumulator.ThinkingAccumulator;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ThinkingBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.ChatUsage;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.transport.HttpTransportException;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.DeepSeekFormatter;
import io.github.xiaou61.ai.AssistantConversation;
import io.github.xiaou61.ai.ConversationTurn;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.internal.provider.GuardedHttpTransport;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 用 AgentScope 承载多轮对话（流式）。
 *
 * <p><b>只多用了它的"流式"这一面</b>，仍然没有 agent 运行时、没有工具、没有它的记忆抽象——
 * 会话与记忆都是本平台自己的表（见 design 的"推荐方案"）。比 {@code AgentScopeEntrySummarizer}
 * 多出来的只有三件事：{@code stream(true)}、把响应里的 {@link ThinkingBlock} 与 {@link TextBlock}
 * **分成两路**回调、以及不设 {@code maxTokens} 的小气上限。
 *
 * <p><b>增量怎么算</b>：用框架自带的 {@link TextAccumulator} / {@link ThinkingAccumulator} 累积每块，
 * 每来一块就把"累积文本比上次多出来的后缀"作为增量回调出去。不自己猜上游是"发增量"还是
 * "发累积快照"——那两个累加器就是 AgentScope 处理流式时的既定做法，跟着它走比自己判断可靠。
 *
 * <p><b>思考与正文分开</b>是需求（AC-002）：两类内容在协议层面就是不同的块类型，也走不同的回调，
 * 界面才能把思考折叠起来而不与正文混淆。
 */
@Component
class AgentScopeConversation implements AssistantConversation {

    private final ConversationProperties properties;
    private final GuardedHttpTransport transport;

    AgentScopeConversation(ConversationProperties properties, GuardedHttpTransport transport) {
        this.properties = properties;
        this.transport = transport;
    }

    @Override
    public void stream(Request request, ModelAccess access, Listener listener) {
        GenerateOptions options = GenerateOptions.builder()
                .modelName(access.model())
                .stream(true)
                .temperature(properties.getTemperature())
                .maxTokens(properties.getMaxTokens())
                .build();
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(access.apiKey())
                .baseUrl(access.baseUrl())
                .modelName(access.model())
                .formatter(new DeepSeekFormatter())
                .stream(true)
                .generateOptions(options)
                // 本轮没有工具。显式关掉这两个开关，免得将来加工具时才发现 DeepSeek
                // "工具与结构化输出不可并用"这条限制（与精简那份同一处声明）。
                .nativeStructuredOutput(false)
                .nativeStructuredOutputWithTools(false)
                .httpTransport(transport)
                .build();

        TextAccumulator content = new TextAccumulator();
        ThinkingAccumulator thinking = new ThinkingAccumulator();
        int[] emitted = {0, 0};

        try {
            for (ChatResponse response : model.stream(toMessages(request), List.of(), options)
                    .timeout(Duration.ofSeconds(properties.getRequestTimeoutSeconds()))
                    .toIterable()) {
                if (response.getContent() == null) {
                    continue;
                }
                for (ContentBlock block : response.getContent()) {
                    if (block instanceof ThinkingBlock block0) {
                        thinking.add(block0);
                        emit(thinking.getAccumulated(), emitted, 0, listener::onThinking);
                    } else if (block instanceof TextBlock block0) {
                        content.add(block0);
                        emit(content.getAccumulated(), emitted, 1, listener::onContent);
                    }
                }
                if (isFinished(response)) {
                    listener.onCompleted(toUsage(response.getUsage()));
                    return;
                }
            }
            // 流正常结束但没有结束事件：按正常结束处理，用量拿不到就给 null
            listener.onCompleted(null);
        } catch (HttpTransportException exception) {
            listener.onFailed(toUpstreamFailure(exception));
        } catch (RuntimeException exception) {
            listener.onFailed(upstreamUnavailable(exception));
        }
    }

    /** 只发"比上次多出来的那一段"。上游发了多少块就回调多少次，不必等结尾。 */
    private static void emit(String accumulated, int[] emitted, int slot, java.util.function.Consumer<String> sink) {
        int seen = emitted[slot];
        if (accumulated.length() <= seen) {
            return;
        }
        emitted[slot] = accumulated.length();
        sink.accept(accumulated.substring(seen));
    }

    private static boolean isFinished(ChatResponse response) {
        String reason = response.getFinishReason();
        return reason != null && !reason.isBlank();
    }

    private List<Msg> toMessages(Request request) {
        List<Msg> messages = new ArrayList<>();
        messages.add(Msg.builder().role(MsgRole.SYSTEM).textContent(ConversationPrompt.system()).build());
        if (request.hasEntryContext()) {
            // 条目正文放系统侧：放用户侧会被模型当成"用户说的话"
            messages.add(Msg.builder()
                    .role(MsgRole.SYSTEM)
                    .textContent(ConversationPrompt.entryContext(request.entryTitle(), request.entryBody()))
                    .build());
        }
        for (ConversationTurn turn : ConversationPrompt.history(request.history())) {
            messages.add(Msg.builder()
                    .role(turn.role() == ConversationTurn.Role.ASSISTANT ? MsgRole.ASSISTANT : MsgRole.USER)
                    .textContent(turn.content())
                    .build());
        }
        messages.add(Msg.builder().role(MsgRole.USER).textContent(request.userMessage()).build());
        return messages;
    }

    private static TokenUsage toUsage(ChatUsage usage) {
        return usage == null
                ? null
                : new TokenUsage(usage.getInputTokens(), usage.getOutputTokens(), usage.getTotalTokens());
    }

    /** 与精简那份同一套分类：只按"谁的问题"给说法，**不复述上游报文**（可能回显 Authorization）。 */
    private static BizException toUpstreamFailure(HttpTransportException exception) {
        String message = exception.getMessage() == null ? "" : exception.getMessage();
        if (message.contains("401") || message.contains("403")) {
            return new BizException(ErrorCode.INVALID_ARGUMENT, "上游拒绝了这组凭据，请检查 API Key 与 baseUrl");
        }
        return upstreamUnavailable(exception);
    }

    private static BizException upstreamUnavailable(Throwable cause) {
        BizException failure = new BizException(ErrorCode.INTERNAL, "上游模型服务暂时不可用，请稍后再试");
        if (cause != null) {
            failure.initCause(cause);
        }
        return failure;
    }
}
