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
import io.github.xiaou61.ai.EntrySummarizer;
import io.github.xiaou61.ai.GeneratedSummary;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.SummaryCard;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.internal.credential.AiProperties;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 用 AgentScope 完成一次精简调用。
 *
 * <p><b>只用它的最小面</b>：一个 OpenAI 兼容模型 + DeepSeek formatter + 一次非流式调用。
 * 没有 agent 运行时（不需要工具、记忆、多轮——那些属 WORK-006）。这样做的好处是依赖虽新、
 * 暴露面却很小，将来升级或换实现只影响这个类。
 *
 * <p>两处刻意的设置：
 * <ul>
 *   <li>{@code nativeStructuredOutput(false)} / {@code nativeStructuredOutputWithTools(false)}：
 *       DeepSeek 不支持原生结构化输出，且它"工具与结构化输出同时出现时会跳过工具调用"。
 *       本项没有工具，写死这两个开关是把这条已知限制变成显式声明，而不是等将来加工具时才发现。</li>
 *   <li><b>不开启思考模式</b>：思考 token 会占用 {@code maxTokens}，吃满时上游**静默返回空**。
 *       摘要是短产物，思考的收益抵不上这个坑。要开就必须同时抬高 {@code maxTokens}
 *       并分开处理两类事件（流式时才需要）。</li>
 * </ul>
 *
 * <p>调用是**同步**的（{@code blockLast}）：一次请求换一次响应，没有中间的增量可做别的事；
 * 前台等它的这段时间由 HTTP 客户端超时兜住。
 */
@Component
class AgentScopeEntrySummarizer implements EntrySummarizer {

    private final AiProperties properties;
    private final SummarySchemaValidator validator;
    private final GuardedHttpTransport transport;

    AgentScopeEntrySummarizer(AiProperties properties,
                              SummarySchemaValidator validator,
                              GuardedHttpTransport transport) {
        this.properties = properties;
        this.validator = validator;
        this.transport = transport;
    }

    @Override
    public GeneratedSummary summarize(String title, String body, ModelAccess access) {
        TruncatedInput input = TruncatedInput.of(body, properties.getMaxInputChars());

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
                Msg.builder().role(MsgRole.SYSTEM).textContent(SummaryPrompt.system()).build(),
                Msg.builder().role(MsgRole.USER).textContent(SummaryPrompt.user(title, input.text())).build());

        ChatResponse response;
        try {
            response = model.stream(messages, List.of(), options)
                    .blockLast(Duration.ofSeconds(properties.getRequestTimeoutSeconds()));
        } catch (HttpTransportException exception) {
            throw toUpstreamFailure(exception);
        } catch (RuntimeException exception) {
            // 超时、连接中断、SDK 内部异常：一律归到"上游不可用"，且**不回显原始报文**
            throw upstreamUnavailable(exception);
        }
        if (response == null) {
            throw upstreamUnavailable(null);
        }

        String text = extractText(response);
        SummaryCard card = validator.validate(text);
        return new GeneratedSummary(card, toUsage(response.getUsage()), input.truncated());
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

    /**
     * 上游的错误分类。刻意不复述上游报文：它可能回显请求头（含 Authorization），
     * 也可能带内部地址。这里只按"谁的问题"给两种说法。
     */
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

    /** 超长正文按段落截断；返回是否截断过，供界面标注"已按前 N 字生成"。 */
    private record TruncatedInput(String text, boolean truncated) {

        static TruncatedInput of(String body, int maxChars) {
            if (body == null) {
                return new TruncatedInput("", false);
            }
            if (body.length() <= maxChars) {
                return new TruncatedInput(body, false);
            }
            String cut = body.substring(0, maxChars);
            int lastParagraph = cut.lastIndexOf("\n\n");
            if (lastParagraph > maxChars / 2) {
                cut = cut.substring(0, lastParagraph);
            }
            return new TruncatedInput(cut, true);
        }
    }
}
