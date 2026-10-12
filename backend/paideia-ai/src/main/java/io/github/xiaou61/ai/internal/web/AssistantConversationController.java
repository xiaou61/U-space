package io.github.xiaou61.ai.internal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xiaou61.ai.AssistantConversation;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.internal.conversation.ConversationService;
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
@RestController
@RequestMapping("/api/v1/ai/conversations")
class AssistantConversationController {

    /** 建会话。`entryId` 可空——助手在任意页都能开，不非得绑在某个条目上。 */
    record CreateRequest(Long entryId) {
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
    private final ObjectMapper json = new ObjectMapper();

    AssistantConversationController(ConversationService conversations,
                                    AssistantConversation assistant,
                                    @Qualifier("applicationTaskExecutor") TaskExecutor executor) {
        this.conversations = conversations;
        this.assistant = assistant;
        this.executor = executor;
    }

    @PostMapping
    ApiResponse<ConversationService.ConversationView> create(@RequestBody(required = false) CreateRequest request) {
        long userId = AiRequester.require();
        Long entryId = request == null ? null : request.entryId();
        long id = conversations.create(userId, entryId);
        return ApiResponse.ok(new ConversationService.ConversationView(id, entryId, null));
    }

    @GetMapping
    ApiResponse<List<ConversationService.ConversationView>> list() {
        return ApiResponse.ok(conversations.list(AiRequester.require()));
    }

    @PostMapping(value = "/{id}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter send(@PathVariable("id") long conversationId, @RequestBody MessageRequest request) {
        long userId = AiRequester.require();
        // 同步阶段：这里抛出的错误会走全局异常处理，得到正常的 400/404 状态码与 ApiResponse 体
        ConversationService.TurnPreparation turn =
                conversations.prepare(userId, conversationId, request == null ? null : request.message());

        SseEmitter emitter = new SseEmitter((long) 100_000);
        executor.execute(() -> runTurn(userId, conversationId, turn, emitter));
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
        try {
            assistant.stream(
                    new AssistantConversation.Request(turn.history(), turn.userMessage(),
                            turn.entryTitle(), turn.entryBody(), turn.memorySnippets()),
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
                        public void onCompleted(TokenUsage usage) {
                            boolean noContent = content.toString().isBlank();
                            String text = noContent ? EMPTY_CONTENT_HINT : content.toString();
                            if (noContent) {
                                // 提示也要作为正文增量发出去：只发 done 的话前端会留一个空气泡，
                                // 用户看到的是"助手什么都没说"，而不是"这次没产出正文"
                                send(emitter, "content", Map.of("delta", EMPTY_CONTENT_HINT));
                            }
                            conversations.complete(userId, conversationId, text, usage);
                            // empty=true 告诉前端"这段是提示，不是模型生成的正文"，免得把它当成回答渲染。
                            // 用 HashMap 而不是 Map.of：用量允许为 null，而 Map.of 见到 null 直接抛。
                            Map<String, Object> payload = new HashMap<>();
                            payload.put("usage", usagePayload(usage));
                            payload.put("empty", noContent);
                            send(emitter, "done", payload);
                            emitter.complete();
                        }

                        @Override
                        public void onFailed(BizException failure) {
                            send(emitter, "error", Map.of("message", failure.getMessage() == null
                                    ? "生成失败，请稍后再试" : failure.getMessage()));
                            emitter.complete();
                        }
                    });
        } catch (RuntimeException unexpected) {
            // 端口约定失败走 onFailed；真出了别的运行时异常也不能让线程静默死掉（客户端会一直等）
            send(emitter, "error", Map.of("message", "生成失败，请稍后再试"));
            emitter.complete();
        }
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
