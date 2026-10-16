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
import io.github.xiaou61.ai.MemoryCandidate;
import io.github.xiaou61.ai.MemoryExtractor;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.internal.memory.MemoryCandidateParser;
import io.github.xiaou61.ai.internal.memory.MemoryExtractionPrompt;
import io.github.xiaou61.ai.internal.memory.MemoryProperties;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 用 AgentScope 完成一次记忆整理调用（WORK-013）。
 *
 * <p>与 {@link AgentScopeEntrySummarizer} 同一套最小面：一个 OpenAI 兼容模型 + DeepSeek formatter
 * + 一次**非流式**调用，没有 agent 运行时。三条刻意设置也照抄那份，理由相同：
 * <ul>
 *   <li>{@code nativeStructuredOutput(false)} / {@code nativeStructuredOutputWithTools(false)}：
 *       DeepSeek 不支持原生结构化输出，且"工具与结构化输出同时出现时会跳过工具调用"。
 *       本项没有工具，写死这两个开关是把已知限制变成显式声明。</li>
 *   <li><b>不开启思考模式</b>：思考 token 会占用 {@code maxTokens}，吃满时上游**静默返回空**。
 *       整理是短产物（一段 JSON），思考的收益抵不上这个坑。</li>
 * </ul>
 *
 * <p>调用是**同步**的（{@code blockLast}）：一次请求换一次响应，且它整个跑在回答之后的异步线程里，
 * 不占用户时间。
 */
@Component
class AgentScopeMemoryExtractor implements MemoryExtractor {

    private final MemoryProperties properties;
    private final MemoryCandidateParser parser;
    private final GuardedHttpTransport transport;

    AgentScopeMemoryExtractor(MemoryProperties properties,
                              MemoryCandidateParser parser,
                              GuardedHttpTransport transport) {
        this.properties = properties;
        this.parser = parser;
        this.transport = transport;
    }

    @Override
    public MemoryExtraction extract(MemoryExtractionRequest request, ModelAccess access) {
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

        List<Msg> messages = List.of(
                Msg.builder().role(MsgRole.SYSTEM).textContent(MemoryExtractionPrompt.system()).build(),
                Msg.builder().role(MsgRole.USER)
                        .textContent(MemoryExtractionPrompt.user(request.memories(), request.turns()))
                        .build());

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

        List<MemoryCandidate> candidates = parser.parse(extractText(response));
        return new MemoryExtraction(candidates, toUsage(response.getUsage()));
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
