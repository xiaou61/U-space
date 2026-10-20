package io.github.xiaou61.ai.internal.essay;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.transport.HttpTransportException;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.DeepSeekFormatter;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.internal.provider.GuardedHttpTransport;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 用 AgentScope 完成一次"面经抽题"调用（WORK-025）。
 *
 * <p>与 {@code AgentScopeMemoryExtractor} 同一套最小面：一个 OpenAI 兼容模型 + DeepSeek formatter
 * + 一次非流式调用，没有 agent 运行时。三条刻意设置也照抄那份，理由相同：
 * <ul>
 *   <li>{@code nativeStructuredOutput(false)}：DeepSeek 不支持原生结构化输出，且"工具与结构化
 *       输出同时出现时会跳过工具调用"。本项没有工具，写死这两个开关是把已知限制变成显式声明。</li>
 *   <li>**思考关不掉**：deepseek-flash 默认就思考，思考 token 会占用 {@code maxTokens}，
 *       吃满时上游会把 JSON 截成半截或静默返回空。故 maxTokens 给足。</li>
 * </ul>
 *
 * <p>调用是同步的（{@code blockLast}）：它整个跑在定时任务线程里，不占用户时间。
 */
@Component
class EssayExtractor {

    /**
     * 单篇面经一次抽题的 token 上限。
     *
     * <p>**实测结论：给小了必然截断**。deepseek-flash 默认思考，思考 token 与正文共用这个预算：
     * 4096 时输出被截在 JSON 中间（`Unterminated string`），8192 同样被吃满。真正让它稳定的是
     * **提示词里把输出规模压到"一道题、300 字内"**（实测输出 665 tokens、4.1 秒、`finish=stop`），
     * 这里的 16384 是给它留的安全余量而不是常规用量。
     */
    private static final int MAX_TOKENS = 16384;

    /** 判断类任务偏稳，但要写解析，不能太低。 */
    private static final double TEMPERATURE = 0.3;

    private final EssayQuestionParser parser;
    private final GuardedHttpTransport transport;

    EssayExtractor(EssayQuestionParser parser, GuardedHttpTransport transport) {
        this.parser = parser;
        this.transport = transport;
    }

    EssayExtractionPrompt.Extraction extract(FetchedPost post, ModelAccess access, int timeoutSeconds) {
        GenerateOptions options = GenerateOptions.builder()
                .modelName(access.model())
                .stream(false)
                .temperature(TEMPERATURE)
                .maxTokens(MAX_TOKENS)
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
                Msg.builder().role(MsgRole.SYSTEM).textContent(EssayExtractionPrompt.system()).build(),
                Msg.builder().role(MsgRole.USER)
                        .textContent(EssayExtractionPrompt.user(post.title(), post.body()))
                        .build());

        ChatResponse response;
        try {
            response = model.stream(messages, List.of(), options)
                    .blockLast(Duration.ofSeconds(timeoutSeconds));
        } catch (HttpTransportException exception) {
            throw toUpstreamFailure(exception);
        } catch (RuntimeException exception) {
            throw upstreamUnavailable(exception);
        }
        if (response == null) {
            throw upstreamUnavailable(null);
        }
        return parser.parse(extractText(response));
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

    /** 上游错误分类，刻意不复述上游报文（它可能回显 Authorization 头或内部地址）。 */
    private static BizException toUpstreamFailure(HttpTransportException exception) {
        String message = exception.getMessage() == null ? "" : exception.getMessage();
        if (message.contains("401") || message.contains("403")) {
            return new BizException(ErrorCode.INVALID_ARGUMENT, "上游拒绝了这组凭据");
        }
        return upstreamUnavailable(exception);
    }

    private static BizException upstreamUnavailable(Throwable cause) {
        BizException failure = new BizException(ErrorCode.INTERNAL, "上游模型服务暂时不可用");
        if (cause != null) {
            failure.initCause(cause);
        }
        return failure;
    }
}
