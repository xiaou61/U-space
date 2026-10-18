package io.github.xiaou61.ai.internal.provider;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.ChatUsage;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.transport.HttpTransportException;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.DeepSeekFormatter;
import io.github.xiaou61.ai.InterviewReviewer;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.internal.conversation.InterviewEvaluationParser;
import io.github.xiaou61.ai.internal.conversation.ReviewPrompt;
import io.github.xiaou61.ai.internal.credential.AiProperties;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 用 AgentScope 完成一次面试点评（WORK-010 T-10）。
 *
 * <p>与 {@link AgentScopeEntrySummarizer} 同一套最小面：一个 OpenAI 兼容模型 + DeepSeek formatter
 * + 一次**非流式**调用，**没有任何工具**。三条刻意设置的理由与那份相同：
 * <ul>
 *   <li>{@code nativeStructuredOutput(false)}：DeepSeek 不支持原生结构化输出；
 *       我们改用"要求模型只回 JSON + 本地严格校验"那套（{@link InterviewEvaluationParser}）。</li>
 *   <li><b>不开启思考模式</b>：思考 token 会占用 {@code maxTokens}，吃满时上游**静默返回空**，
 *       而"空"在这里会被当成一次失败（不落库），用户会看到"没拿到评估"。</li>
 *   <li>{@code stream(false)}：点评是一次性结果，没有逐步显示的需求。</li>
 * </ul>
 *
 * <p>**上下文长度**：一场面试的对话可能很长。这里**不截断**——截断会丢掉后半场的表现，
 * 而评分正是基于整场。代理由 `maxTokens` 与模型自身的窗口兜住；真撞到窗口上限时上游会拒绝，
 * 那会以"上游不可用"报出来，不会静默给出半场评分。
 */
@Component
class AgentScopeInterviewReviewer implements InterviewReviewer {

    private final AiProperties properties;
    private final InterviewEvaluationParser parser;
    private final GuardedHttpTransport transport;

    AgentScopeInterviewReviewer(AiProperties properties,
                                InterviewEvaluationParser parser,
                                GuardedHttpTransport transport) {
        this.properties = properties;
        this.parser = parser;
        this.transport = transport;
    }

    @Override
    public GeneratedReview review(String transcript, ModelAccess access) {
        GenerateOptions options = GenerateOptions.builder()
                .modelName(access.model())
                .stream(false)
                .temperature(properties.getTemperature())
                .maxTokens(properties.getMaxTokens())
                .build();
        OpenAIChatModel model = OpenAIChatModel.builder()
                .apiKey(access.apiKey())
                .baseUrl(access.baseUrl())
                .modelName(access.model())
                .formatter(new DeepSeekFormatter())
                .stream(false)
                .generateOptions(options)
                .nativeStructuredOutput(false)
                .nativeStructuredOutputWithTools(false)
                .httpTransport(transport)
                .build();

        // **不挂工具**：这次要的是结构化评分，而 DeepSeek 上工具与结构化输出不能同一次调用并用。
        // 这也是 T-06/AC-007 要求"点评的工具集为空"的落点。
        List<Msg> messages = List.of(
                Msg.builder().role(MsgRole.SYSTEM).textContent(ReviewPrompt.system()).build(),
                Msg.builder().role(MsgRole.USER).textContent(ReviewPrompt.user(transcript)).build());

        ChatResponse response;
        try {
            response = model.stream(messages, List.of(), options)
                    .blockLast(Duration.ofSeconds(properties.getRequestTimeoutSeconds()));
        } catch (HttpTransportException exception) {
            throw toUpstreamFailure(exception);
        } catch (RuntimeException exception) {
            throw upstreamUnavailable(exception);
        }
        if (response == null) {
            throw upstreamUnavailable(null);
        }

        return new GeneratedReview(parser.parse(extractText(response)), toUsage(response.getUsage()));
    }

    private static String extractText(ChatResponse response) {
        StringBuilder builder = new StringBuilder();
        if (response.getContent() != null) {
            for (var block : response.getContent()) {
                if (block instanceof TextBlock textBlock) {
                    builder.append(textBlock.getText());
                }
            }
        }
        return builder.toString();
    }

    private static TokenUsage toUsage(ChatUsage usage) {
        return usage == null
                ? null
                : new TokenUsage(usage.getInputTokens(), usage.getOutputTokens(), usage.getTotalTokens());
    }

    /** 上游错误分类，刻意不复述上游报文（它可能回显 Authorization 头或内部地址）。 */
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
