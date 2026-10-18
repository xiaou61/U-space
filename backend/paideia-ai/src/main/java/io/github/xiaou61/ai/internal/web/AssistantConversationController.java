package io.github.xiaou61.ai.internal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xiaou61.ai.AssistantConversation;
import io.github.xiaou61.ai.Harness;
import io.github.xiaou61.ai.NoteDraft;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.internal.billing.CreditService;
import io.github.xiaou61.ai.internal.conversation.ConversationService;
import io.github.xiaou61.ai.internal.memory.MemoryExtractionService;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import io.github.xiaou61.platform.ApiAccess;

/**
 * 助手对话端点。
 *
 * <p><b>三个动作分开</b>：建会话、列会话是普通 JSON；只有"发一轮消息"是 SSE，
 * 因为只有它会持续产生增量。
 *
 * <p><b>所有校验都在开流之前同步做完</b>（归属、空消息、凭据缺失）。SSE 一旦开始就无法再改
 * HTTP 状态码——把 403/404 混进事件流里，调用方要解析事件才知道失败，那不叫 REST。
 * 开流之后只剩"模型侧失败"，那才是事件。
 *
 * <p><b>事件负载一律是 JSON</b>：SSE 的 {@code data:} 行不能含裸换行，正文里必然有。
 * 前端把每个事件 parse 一次即可，不必自己处理转义。
 *
 * <p>事件：{@code thinking}（思考增量）、{@code content}（正文增量）、
 * {@code done}（带用量，可能为 null）、{@code error}（带可见消息）。{@code done} 与 {@code error} 互斥且各一次。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/ai/conversations")
class AssistantConversationController {

    /** 建会话。`entryId` 可空——助手在任意页都能开，不非得绑在某个条目上。 */
    /**
     * @param harness 这段会话用哪套公共行为（WORK-010）。不传 = 通用助手；
     *                面试页传 `interview`。**会话建好之后行为就跟着会话走**（恢复时按它选）。
     */
    record CreateRequest(Long entryId, String harness) {
    }

    record MessageRequest(String message) {
    }

    /**
     * 只有思考、正文为空时的可见提示。
     *
     * <p>这是 DeepSeek 那个坑的界面侧兜底：思考 token 吃满 {@code maxTokens} 时上游**静默返回空正文**。
     * 显示一个空气泡会让人以为助手坏了，说清楚原因才有用。
     */
    static final String EMPTY_CONTENT_HINT =
            "这次只产生了思考过程，没有生成正文。可以重试，或在设置里换一个 max_tokens 更宽裕的模型。";

    private final ConversationService conversations;
    private final AssistantConversation assistant;
    private final TaskExecutor executor;
    private final CreditService credits;
    private final MemoryExtractionService memoryExtraction;
    private final ObjectMapper json = new ObjectMapper();

    AssistantConversationController(ConversationService conversations,
                                    AssistantConversation assistant,
                                    @Qualifier("applicationTaskExecutor") TaskExecutor executor,
                                    CreditService credits,
                                    MemoryExtractionService memoryExtraction) {
        this.conversations = conversations;
        this.assistant = assistant;
        this.executor = executor;
        this.credits = credits;
        this.memoryExtraction = memoryExtraction;
    }

    @PostMapping
    ApiResponse<ConversationService.ConversationView> create(@RequestBody(required = false) CreateRequest request) {
        long userId = AiRequester.require();
        Long entryId = request == null ? null : request.entryId();
        Harness harness = Harness.ofOrDefault(request == null ? null : request.harness());
        long id = conversations.create(userId, entryId, harness);
        return ApiResponse.ok(new ConversationService.ConversationView(id, entryId, null));
    }

    @GetMapping
    ApiResponse<List<ConversationService.ConversationView>> list() {
        return ApiResponse.ok(conversations.list(AiRequester.require()));
    }

    /**
     * 读回某会话的消息（旧 → 新），供界面恢复历史（WORK-011）。
     *
     * <p>归属校验在服务层按 SQL 做：不存在 / 不是本人的 / 已过期**都是同一条 404**——
     * 区分开就等于确认"某个会话存在"。条数受库里保留上限约束，不无限拉。
     */
    @GetMapping("/{id}/messages")
    ApiResponse<List<ConversationService.MessageView>> messages(@PathVariable("id") long conversationId) {
        return ApiResponse.ok(conversations.readMessages(AiRequester.require(), conversationId));
    }

    @PostMapping(value = "/{id}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter send(@PathVariable("id") long conversationId, @RequestBody MessageRequest request) {
        long userId = AiRequester.require();
        // 同步阶段：这里抛出的错误会走全局异常处理，得到正常的 400/404 状态码与 ApiResponse 体
        ConversationService.TurnPreparation turn =
                conversations.prepare(userId, conversationId, request == null ? null : request.message());

        SseEmitter emitter = new SseEmitter((long) 100_000);
        try {
            executor.execute(() -> runTurn(userId, conversationId, turn, emitter));
        } catch (RuntimeException cause) {
            // 线程池满/已关闭时任务根本没跑起来：这笔预留到不了 settleOnce、也没有别的补偿出口，
            // 用户会被白扣一次分。全额退还再抛出——同步阶段的异常能走全局异常处理拿到正常状态码。
            settleOnce(new boolean[1], userId, turn, null, null);
            throw cause;
        }
        return emitter;
    }

    /**
     * 在独立线程里消费上游流并推送事件。
     *
     * <p>助手消息**只在成功时落库**：失败轮次不写，也就不进后续上下文（失败的回答不该被记住）。
     * 用户那条消息在 {@code prepare} 里已经写了，所以失败不会丢掉用户说过的话。
     */
    private void runTurn(long userId, long conversationId,
                         ConversationService.TurnPreparation turn, SseEmitter emitter) {
        StringBuilder content = new StringBuilder();
        // 用量单独存一格：onUsage 先于结果回调，这样"失败也要为已烧掉的 token 负责"不必在每条错误路径上再判一次
        TokenUsage[] usageRef = new TokenUsage[1];
        // 结算只做一次的闸（理由见 settleOnce）
        boolean[] settled = {false};
        try {
            assistant.stream(
                    new AssistantConversation.Request(userId, turn.history(), turn.userMessage(),
                            turn.entryTitle(), turn.entryBody(), turn.memorySnippets(), turn.harness()),
                    turn.access(),
                    new AssistantConversation.Listener() {
                        @Override
                        public void onThinking(String delta) {
                            send(emitter, "thinking", Map.of("delta", delta));
                        }

                        @Override
                        public void onContent(String delta) {
                            content.append(delta);
                            send(emitter, "content", Map.of("delta", delta));
                        }

                        @Override
                        public void onDraft(NoteDraft draft) {
                            // 草稿走独立事件：前端据此显示"要不要存成笔记"的确认条，
                            // 而不是把它当正文的一部分。落库要等用户点击（另一个端点）。
                            Map<String, Object> payload = new HashMap<>();
                            payload.put("title", draft.title());
                            payload.put("body", draft.body());
                            send(emitter, "draft", payload);
                        }

                        @Override
                        public void onUsage(TokenUsage usage) {
                            // 只赋值，不做任何可能抛异常的 I/O：它先于 onCompleted/onFailed 被调用，
                            // 这里抛出去会让用户既拿不到结果、也拿不到错误。
                            usageRef[0] = usage;
                        }

                        @Override
                        public void onCompleted() {
                            TokenUsage usage = usageRef[0];
                            boolean noContent = content.toString().isBlank();
                            String text = noContent ? EMPTY_CONTENT_HINT : content.toString();
                            if (noContent) {
                                // 提示也要作为正文增量发出去：只发 done 的话前端会留一个空气泡，
                                // 用户看到的是"助手什么都没说"，而不是"这次没产出正文"
                                send(emitter, "content", Map.of("delta", EMPTY_CONTENT_HINT));
                            }
                            // 先落消息（拿到 id）、再按这个 id 结算：流水的 ref_message_id 能对上那条消息，
                            // "逐条可对账"（AC-006）才有依据
                            long messageId = conversations.complete(userId, conversationId, text, usage);
                            settleOnce(settled, userId, turn, usage, messageId);
                            // empty=true 告诉前端"这段是提示，不是模型生成的正文"，免得把它当成回答渲染。
                            // 用 HashMap 而不是 Map.of：用量允许为 null，而 Map.of 见到 null 直接抛。
                            Map<String, Object> payload = new HashMap<>();
                            payload.put("usage", usagePayload(usage));
                            payload.put("empty", noContent);
                            send(emitter, "done", payload);
                            emitter.complete();
                            // 自动整理记忆的**触发点**（WORK-013）：刻意放在 emitter.complete()
                            // **之后**——用户这时已经拿到完整回答，这一步慢或失败都与他的体验无关。
                            // 它自己只做一次极轻的判断；真干活的部分由它内部丢到另一个线程。
                            memoryExtraction.scheduleIfDue(userId, conversationId);
                        }

                        @Override
                        public void onFailed(BizException failure) {
                            // 失败**也要过账**：用量拿到了就按用量扣，没拿到就全额退回。
                            // "失败不落库"只管消息，钱那一步必须显式处理。
                            settleOnce(settled, userId, turn, usageRef[0], null);
                            send(emitter, "error", Map.of("message", failure.getMessage() == null
                                    ? "生成失败，请稍后再试" : failure.getMessage()));
                            emitter.complete();
                        }
                    });
        } catch (RuntimeException unexpected) {
            // 端口约定失败走 onFailed；真出了别的运行时异常也不能让线程静默死掉（客户端会一直等）
            settleOnce(settled, userId, turn, null, null);
            send(emitter, "error", Map.of("message", "生成失败，请稍后再试"));
            emitter.complete();
        }
    }

    /**
     * 结算或退还这一轮的预留，**且只做一次**。
     *
     * <p>为什么要有"只做一次"的闸：`onCompleted` 里先结算、再往 SSE 写 `done`，而写事件可能抛
     * （客户端断开）。那会落到外层的 `catch`，于是同一次调用被退两遍——那是白送钱。
     * 用一个布尔闸比"分析每条路径有没有重叠"可靠。
     *
     * <p><b>拿不到用量就全额退</b>：没有用量就没有扣费依据。宁可漏扣（审计页上"有用量但 0 扣减"
     * 一眼可见），也不要凭猜扣用户的分。
     */
    private void settleOnce(boolean[] settled, long userId, ConversationService.TurnPreparation turn,
                            TokenUsage usage, Long messageId) {
        if (settled[0]) {
            return;
        }
        settled[0] = true;
        if (usage == null) {
            credits.refund(userId, turn.reservedCredits(), messageId, "上游未返回用量");
            return;
        }
        credits.settle(userId, turn.reservedCredits(), turn.access().model(),
                usage.inputTokens(), usage.outputTokens(), messageId);
    }

    private static Map<String, Object> usagePayload(TokenUsage usage) {
        return usage == null
                ? null
                : Map.of("input", usage.inputTokens(), "output", usage.outputTokens(), "total", usage.totalTokens());
    }

    /** 客户端断开是常态（用户关页面），不该把线程炸掉；写失败就当作这轮结束。 */
    private void send(SseEmitter emitter, String name, Object payload) {
        try {
            emitter.send(SseEmitter.event().name(name).data(json.writeValueAsString(payload)));
        } catch (IOException | IllegalStateException exception) {
            throw new BizException(ErrorCode.INTERNAL, "连接已断开");
        }
    }
}
