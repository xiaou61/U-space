package io.github.xiaou61.ai;

import io.github.xiaou61.platform.BizException;
import java.util.List;

/**
 * 对话端口：一轮多轮对话，产出**逐段**的思考与正文。
 *
 * <p><b>签名里没有任何框架类型</b>（D-09 的要求，与 {@link EntrySummarizer} 同一纪律）：
 * 换模型实现、升级 AgentScope 都不动调用方。实现放在 {@code internal.conversation}。
 *
 * <p>端口只负责"给我上下文与凭据，我把这一轮的增量吐给你"：**不落库、不管会话归属、
 * 不做限流**——那些是调用方的事。因此它可以被桩替换，测试里不碰网络。
 *
 * <p>为什么用回调而不是返回一个流对象：本项目跑在 Spring MVC 上（不是 WebFlux），
 * 调用方是 {@code SseEmitter}；回调让"消费上游流"这件事留在实现内部，
 * 端口不必泄漏任何响应式类型。
 *
 * <p>失败语义：上游不可用、被拒、返回空内容，一律走 {@link Listener#onFailed(BizException)}，
 * **且不回显上游原始报文**。调用方不得把失败结果当作正常回复落库。
 */
public interface AssistantConversation {

    /**
     * @param request 这一轮的上下文与用户输入
     * @param access  本次调用用的凭据（明文只在这次调用期间存在，与 {@link EntrySummarizer} 同一纪律）
     * @param listener 增量回调
     */
    void stream(Request request, ModelAccess access, Listener listener);

    /**
     * @param userId         当前调用者的账号 id。**工具读数据只认它**——模型无法提供或改写这个值
     * @param history        之前的对话（旧 → 新），供模型保持多轮上下文
     * @param userMessage    本轮用户输入
     * @param entryTitle     可选：当前条目标题（为空表示这轮没有条目上下文）
     * @param entryBody      可选：当前条目正文；超长由实现截断
     * @param memorySnippets 召回的记忆（本人私有），每条一行；为空表示这次没有可用记忆
     */
    record Request(long userId,
                   List<ConversationTurn> history,
                   String userMessage,
                   String entryTitle,
                   String entryBody,
                   List<String> memorySnippets) {

        public Request {
            history = history == null ? List.of() : List.copyOf(history);
            memorySnippets = memorySnippets == null ? List.of() : List.copyOf(memorySnippets);
        }

        /** 有没有条目上下文。实现据此决定是否把正文放进提示词。 */
        public boolean hasEntryContext() {
            return entryBody != null && !entryBody.isBlank();
        }
    }

    /**
     * 增量回调。
     *
     * <p>两个内容回调**分开**是需求（AC-002）：思考过程要在界面里单独折叠，不能和正文混在一起。
     * {@code onCompleted} 与 {@code onFailed} 互斥，且各自只会被调用一次。
     */
    interface Listener {

        /** 思考过程的增量。模型不产出思考时一次都不会调用。 */
        void onThinking(String delta);

        /** 正文的增量。 */
        void onContent(String delta);

        /**
         * 助手产出了一条**笔记草稿**（不是笔记本身）。
         *
         * <p>由工具参数承载，界面据此显示"要不要存成笔记"；真正落库要等用户点击。
         * 一轮里可能出现多次（模型分几次提）。
         */
        void onDraft(NoteDraft draft);

        /** 正常结束。{@code usage} 可能为 {@code null}（上游没给用量）。 */
        void onCompleted(TokenUsage usage);

        /** 失败。调用方据此给用户可见的结果，并**不**把这轮写进历史。 */
        void onFailed(BizException failure);
    }
}
