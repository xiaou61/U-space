package io.github.xiaou61.ai.internal.conversation;

import io.agentscope.core.agent.accumulator.TextAccumulator;
import io.agentscope.core.agent.accumulator.ToolCallsAccumulator;
import io.agentscope.core.agent.accumulator.ThinkingAccumulator;
import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ThinkingBlock;
import io.agentscope.core.message.ToolResultBlock;
import io.agentscope.core.message.ToolResultMessage;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.ChatUsage;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ToolSchema;
import io.agentscope.core.model.transport.HttpTransportException;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.DeepSeekFormatter;
import io.github.xiaou61.ai.AssistantConversation;
import io.github.xiaou61.ai.Harness;
import io.github.xiaou61.ai.ConversationTurn;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.internal.provider.GuardedHttpTransport;
import io.github.xiaou61.ai.internal.tools.AgentToolBridge;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /**
     * 一轮对话里最多允许多少次"模型要工具、我们回结果"的往返。
     *
     * <p>ponytail: 固定 4 轮。它是防"模型绕着同一个工具转圈把用户的钱烧光"的兜底，
     * 不是功能上限——真出现"4 轮不够"的实测再提成配置。
     */
    private static final int MAX_TOOL_ROUNDS = 4;
    private static final Logger log = LoggerFactory.getLogger(AgentScopeConversation.class);

    private final ConversationProperties properties;
    private final GuardedHttpTransport transport;
    private final AgentToolBridge tools;

    AgentScopeConversation(ConversationProperties properties,
                           GuardedHttpTransport transport,
                           AgentToolBridge tools) {
        this.properties = properties;
        this.transport = transport;
        this.tools = tools;
    }

    @Override
    public void stream(Request request, ModelAccess access, Listener listener) {
        GenerateOptions options = GenerateOptions.builder()
                .modelName(access.model())
                .stream(true)
                .temperature(properties.getTemperature())
                .maxTokens(properties.getMaxTokens())
                .build();
        Model model = buildModel(access, options);

        List<Msg> conversation = new ArrayList<>(toMessages(request));
        // 工具集按 harness 从平台目录取（WORK-019）：点评那次是空的——它要结构化评分，
        // 而 DeepSeek 上工具与结构化输出不能同一次调用并用。
        List<ToolSchema> core = tools.core(request.harness());
        List<ToolSchema> discovery = tools.discoverySchemas();
        // 本请求内已发现的工具名。**每轮重算清单**：find_tools 命中的工具必须在下一次上游调用里
        // 就出现，否则模型会一直重复搜索（DeepSeek 没有端侧工具搜索，这一步只能自己做）。
        List<String> discovered = new ArrayList<>();

        // 逐轮累加：工具循环里**每一轮都是一次真实的上游调用**，都必须计入用量。
        // 原先只把最后一轮上报出去、中间轮次全丢——BYOK 下是"少报给用户看"，
        // 换成平台出钱之后就是**平台漏扣**（WORK-009 T-01）。
        TokenUsage total = null;
        try {
            for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
                List<ToolSchema> schemas = new ArrayList<>(tools.concat(core, discovered));
                // 核心包为空（点评官）时不加发现元工具：它不该有任何工具
                if (!core.isEmpty()) {
                    schemas.addAll(discovery);
                }
                Round roundResult = runRound(model, conversation, schemas, options, listener);
                if (roundResult == null) {
                    // 上游一轮都没吐出东西：按不可用处理
                    listener.onUsage(total);
                    listener.onFailed(upstreamUnavailable(null));
                    return;
                }
                total = add(total, roundResult.usage());
                List<ToolUseBlock> requested = roundResult.toolCalls();
                if (requested.isEmpty()) {
                    // **最终轮**：它的正文才是用户要的答案，一次性推出去。
                    // 之前每轮都实时 emit，导致工具轮的旁白（"我查一下…"，有时甚至是一份完整答案）
                    // 与最终回答拼在一起，用户看到同一段话重复两遍（2026-10-19 实测）。
                    if (roundResult.text() != null && !roundResult.text().isEmpty()) {
                        listener.onContent(roundResult.text());
                    }
                    listener.onUsage(total);
                    listener.onCompleted();
                    return;
                }

                // 让模型看见自己发出的工具调用，再喂回结果——否则下一轮它会重复调用同一个工具
                conversation.add(roundResult.assistantMessage());
                // **每个 tool_call_id 各发一条 ToolResultMessage**（2026-10-19 实测修）：
                // OpenAI 协议要求 assistant 的每个 tool_calls 都有对应的 tool 消息。
                // 用 `new ToolResultMessage(List)` 把多条打包成一条时，formatter 只产出一个 tool
                // 消息，上游直接 400（"insufficient tool messages following tool_calls message"）。
                for (ToolResultBlock result : execute(request.userId(), request.harness(),
                        requested, discovered, listener)) {
                    conversation.add(new ToolResultMessage(result));
                }
            }
            // 兜底：真的绕成了环就停下，别让一次请求无限花用户的钱。
            // **这一条路径也要上报用量**：那几轮的钱是真花了，只是没产出可用结果（原先这里一笔都不记）。
            listener.onUsage(total);
            listener.onFailed(new BizException(ErrorCode.INTERNAL, "助手尝试调用工具的次数过多，已停止"));
        } catch (HttpTransportException exception) {
            // 服务端留完整堆栈：用户只看到一句可读的提示，但"为什么失败"必须能查
            log.warn("assistant.upstream.http-failed userId={}", request.userId(), exception);
            listener.onUsage(total);
            listener.onFailed(toUpstreamFailure(exception));
        } catch (RuntimeException exception) {
            log.warn("assistant.upstream.failed userId={}", request.userId(), exception);
            listener.onUsage(total);
            listener.onFailed(upstreamUnavailable(exception));
        }
    }

    /**
     * 建模型客户端。
     *
     * <p>抽成方法是为了**可替换**：用量累加这件事没法用"桩掉端口"那一套测——桩里根本没有循环。
     * 只能在模型这一层注一个假模型，喂两轮响应（第一轮要工具、第二轮出正文），断言总量是两轮之和。
     */
    Model buildModel(ModelAccess access, GenerateOptions options) {
        return openAiModel(access, options, transport);
    }

    /**
     * 建 OpenAI 兼容模型的公共实现（流式对话与非流式问答共用）。
     *
     * <p>抽成静态方法是为了让两条循环用**完全一样**的模型配置——DeepSeek 那几条坑
     * （思考 token、nativeStructuredOutput 必须关）只写一处，不会有人在一侧漏掉。
     */
    static Model openAiModel(ModelAccess access, GenerateOptions options, GuardedHttpTransport transport) {
        return OpenAIChatModel.builder()
                .apiKey(access.apiKey())
                .baseUrl(access.baseUrl())
                .modelName(access.model())
                .formatter(new DeepSeekFormatter())
                .stream(true)
                .generateOptions(options)
                // 仍显式关掉"原生结构化输出"：本项取结构化结果走**单独一次不带工具**的调用
                // （见 TASK-010 的分流），不在这一层。DeepSeek 上工具与结构化输出不能并用的限制
                // 就靠这个分工绕开。
                .nativeStructuredOutput(false)
                .nativeStructuredOutputWithTools(false)
                .httpTransport(transport)
                .build();
    }

    /** 两段用量相加。任一段缺失（上游没给）时按"另一段"计，两段都缺才是 null。 */
    static TokenUsage add(TokenUsage left, TokenUsage right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return new TokenUsage(left.inputTokens() + right.inputTokens(),
                left.outputTokens() + right.outputTokens(),
                left.totalTokens() + right.totalTokens());
    }

    /**
     * 跑一轮：边收边把思考／正文的增量回调出去，同时累积这一轮请求的工具调用。
     *
     * <p>**每一轮都照常流式**（不是"先非流式试一轮再决定"）：工具的中间说明（"我查一下…"）
     * 也是助手说的话，让用户看着它动比空等更接近流式本来的意思；最终轮的正文就是答案。
     * 代价是中间说明会一起落进这条助手消息——那是"它说过的话"，不是错误。
     */
    private Round runRound(Model model, List<Msg> conversation, List<ToolSchema> schemas,
                           GenerateOptions options, Listener listener) {
        TextAccumulator content = new TextAccumulator();
        ThinkingAccumulator thinking = new ThinkingAccumulator();
        ToolCallsAccumulator calls = new ToolCallsAccumulator();
        int[] emitted = {0};
        ChatResponse last = null;

        for (ChatResponse response : model.stream(conversation, schemas, options)
                .timeout(Duration.ofSeconds(properties.getRequestTimeoutSeconds()))
                .toIterable()) {
            last = response;
            if (response.getContent() == null) {
                continue;
            }
            for (ContentBlock block : response.getContent()) {
                if (block instanceof ThinkingBlock thinkingBlock) {
                    thinking.add(thinkingBlock);
                    emit(thinking.getAccumulated(), emitted, 0, listener::onThinking);
                } else if (block instanceof TextBlock textBlock) {
                    content.add(textBlock);
                } else if (block instanceof ToolUseBlock toolCall) {
                    calls.add(toolCall);
                }
            }
        }
        if (last == null) {
            return null;
        }

        List<ToolUseBlock> requested = calls.buildAllToolCalls();
        List<ContentBlock> blocks = new ArrayList<>();
        // **不要把思考包装成 TextBlock 塞回 assistant 消息**（2026-10-19 实测修）：
        // DeepSeek 的 formatter 看到同一条 assistant 里既有普通文本又有 tool_calls 时，
        // 会以"assistant 正文 + tool_calls"的形式发出；而把我们自己拼的 "[思考] …" 当成正文
        // 会让紧随其后的 tool 消息失去合法配对，上游回 400
        // （"assistant message with 'tool_calls' must be followed by tool messages"）。
        // 思考本来就只用于界面展示，不进历史。
        if (!content.getAccumulated().isEmpty()) {
            blocks.add(TextBlock.builder().text(content.getAccumulated()).build());
        }
        blocks.addAll(requested);
        Msg assistant = Msg.builder().role(MsgRole.ASSISTANT).content(blocks).build();
        return new Round(assistant, content.getAccumulated(), requested, toUsage(last.getUsage()));
    }

    /** 一轮的产物。 */
    private record Round(Msg assistantMessage, String text, List<ToolUseBlock> toolCalls, TokenUsage usage) {
    }

    /**
     * 执行这一轮请求的工具，并包成给模型看的结果。
     *
     * <p>失败**不抛**：把错误文本当成结果交回去（文本里写明"工具执行失败"），
     * 模型据此换个方式回答，比让整轮对话崩掉好。所以这里不额外设置错误状态——
     * 语义已经在那段文本里，多一层状态只会让读的人以为还有别的分支。
     */
    private List<ToolResultBlock> execute(long userId, Harness harness, List<ToolUseBlock> requested,
                                          List<String> discovered, Listener listener) {
        List<ToolResultBlock> results = new ArrayList<>();
        for (ToolUseBlock call : requested) {
            AgentToolBridge.Outcome outcome = tools.run(userId, harness, call.getName(), call.getInput());
            if (outcome.draft() != null) {
                // 草稿立刻送到界面：用户的"要不要存"不该等模型把话说完才出现
                listener.onDraft(outcome.draft());
            }
            // find_tools 的命中要在下一轮生效：看见什么就记下来（重复的不管，concat 会按名字取工具）
            discovered.addAll(outcome.discovered());
            results.add(new ToolResultBlock(call.getId(), call.getName(),
                    TextBlock.builder().text(outcome.text()).build()));
        }
        return results;
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

    private List<Msg> toMessages(Request request) {
        List<Msg> messages = new ArrayList<>();
        // 提示词按 harness 选（WORK-010 T-05）：这是"公共行为"唯一的落点，
        // 上下文/记忆/凭据都不受影响。
        messages.add(Msg.builder().role(MsgRole.SYSTEM)
                .textContent(HarnessPrompts.system(request.harness())).build());
        if (request.hasEntryContext()) {
            // 条目正文放系统侧：放用户侧会被模型当成"用户说的话"
            messages.add(Msg.builder()
                    .role(MsgRole.SYSTEM)
                    .textContent(ConversationPrompt.entryContext(request.entryTitle(), request.entryBody()))
                    .build());
        }
        if (request.harness() == Harness.TUTOR && request.learningSummary() != null
                && !request.learningSummary().isBlank()) {
            // 学情是平台记录的事实（WORK-020）：只注入导师，且明说来源，避免模型把它当成用户自述
            messages.add(Msg.builder()
                    .role(MsgRole.SYSTEM)
                    .textContent(TutorPrompt.learningContext(request.learningSummary()))
                    .build());
        }
        if (!request.memorySnippets().isEmpty()) {
            messages.add(Msg.builder()
                    .role(MsgRole.SYSTEM)
                    .textContent(ConversationPrompt.memory(request.memorySnippets()))
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
