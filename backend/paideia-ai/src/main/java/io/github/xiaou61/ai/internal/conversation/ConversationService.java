package io.github.xiaou61.ai.internal.conversation;

import io.github.xiaou61.ai.ConversationTurn;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.internal.credential.CredentialService;
import io.github.xiaou61.ai.internal.memory.MemoryStore;
import io.github.xiaou61.ai.internal.support.UserRateLimiter;
import io.github.xiaou61.knowledge.KnowledgeApi;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会话的编排：归属校验、上下文装配、写入与裁剪。
 *
 * <p>端口（{@link io.github.xiaou61.ai.AssistantConversation}）只负责"调模型"，这里负责"围绕它的一切"。
 * 分开的好处是模型可以被桩替换，而本类的行为（归属、上限、过期、上下文顺序）能脱离网络单测。
 *
 * <p>三条纪律：
 * <ul>
 *   <li><b>归属一律经 SQL 校验</b>：拿不到（不存在／不是本人的／已过期）就当作"会话不存在"，
 *       对外返回同一个错误，不区分原因——区分开就等于确认"某个会话存在"。</li>
 *   <li><b>用户消息在调模型之前写</b>：模型失败时用户那句话不该丢；助手消息只在成功后才写，
 *       失败轮次不落库（也就不会进后续上下文）。</li>
 *   <li><b>滑动续期与裁剪都在同一处</b>：否则"什么时候会过期""历史为什么变短"会散落成多处。</li>
 * </ul>
 */
@Service
public class ConversationService {

    private final ConversationMapper conversations;
    private final ConversationMessageMapper messages;
    private final ConversationProperties properties;
    private final CredentialService credentials;
    private final KnowledgeApi knowledge;
    private final MemoryStore memories;
    private final UserRateLimiter limiter;

    ConversationService(ConversationMapper conversations,
                        ConversationMessageMapper messages,
                        ConversationProperties properties,
                        CredentialService credentials,
                        KnowledgeApi knowledge,
                        MemoryStore memories,
                        UserRateLimiter limiter) {
        this.conversations = conversations;
        this.messages = messages;
        this.properties = properties;
        this.credentials = credentials;
        this.knowledge = knowledge;
        this.memories = memories;
        this.limiter = limiter;
    }

    /** 会话列表项。**不含消息内容**：列表只用来选，拉开某一条才取内容。 */
    public record ConversationView(long id, Long entryId, Instant updatedAt) {
    }

    /**
     * 一轮对话开始前所需的一切。
     *
     * @param conversationId 已校验归属的会话 id
     * @param history        进上下文的历史（旧 → 新，已按窗口裁剪）
     * @param userMessage    本轮用户输入
     * @param entryTitle     可选条目上下文
     * @param entryBody      可选条目上下文
     * @param memorySnippets 召回的记忆（本人私有，每条一行）
     * @param access         凭据；调用方据此给出"尚未配置"的提示
     */
    public record TurnPreparation(long conversationId,
                                  List<ConversationTurn> history,
                                  String userMessage,
                                  String entryTitle,
                                  String entryBody,
                                  List<String> memorySnippets,
                                  ModelAccess access) {
    }

    public long create(long userId, Long entryId) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setEntryId(entryId);
        conversation.setExpiresAt(Instant.now().plus(properties.getTtl()));
        conversations.insert(conversation);
        return conversation.getId();
    }

    public List<ConversationView> list(long userId) {
        return conversations.listActiveByUser(userId, Instant.now()).stream()
                .map(it -> new ConversationView(it.getId(), it.getEntryId(), it.getUpdatedAt()))
                .toList();
    }

    /**
     * 校验归属、写入用户消息、装配上下文。
     *
     * <p>凭据缺失不算"会话不存在"，所以单独抛一条更明确的错误：界面要据此提示
     * "先配置你自己的模型 Key"，而不是让人以为会话丢了。
     */
    @Transactional
    public TurnPreparation prepare(long userId, long conversationId, String prompt) {
        String trimmed = prompt == null ? "" : prompt.trim();
        if (trimmed.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "消息内容不能为空");
        }
        Instant now = Instant.now();
        Conversation conversation = conversations.findActiveByIdAndUser(conversationId, userId, now)
                .orElseThrow(ConversationService::conversationNotFound);

        Optional<ModelAccess> access = credentials.access(userId);
        if (access.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "尚未配置模型凭据，请先填写你自己的 API Key");
        }
        // 限流放在凭据校验**之后**：没配凭据的请求根本不会打到上游，不该占用户的额度
        // （与"命中缓存不计入"同一个口径）
        limiter.checkAndCount(userId, "发送消息",
                properties.getPerMinuteLimit(), properties.getPerDayLimit());

        appendMessage(conversationId, ConversationMessage.ROLE_USER, trimmed, null);
        // 滑动续期：聊到一半过期比"聊完就丢"更让人费解
        conversations.slideExpiry(conversationId, userId, now.plus(properties.getTtl()), now);

        EntryContext entry = entryContext(conversation.getEntryId());
        return new TurnPreparation(conversationId, history(conversationId), trimmed,
                entry.title(), entry.body(), memorySnippets(userId, trimmed), access.get());
    }

    /**
     * 召回本人记忆并拼成给模型看的几行。
     *
     * <p>用**本轮用户输入**当检索关键词：这一步是"这轮的话可能和过去说过什么有关"，
     * 关键词为空（理论上不会，消息已校验非空）时退回"取最近的记忆"。
     *
     * <p>两路在提示词里**标注来源**：结构化的是"关于学习者的事实"，全文的是"他说过的话"。
     * 不标的话，模型会把"用户曾经随口说的一句"当成确定的事实。
     */
    private List<String> memorySnippets(long userId, String keyword) {
        return memories.recall(userId, keyword).stream()
                .map(item -> (item.source() == MemoryStore.MemoryItem.Source.STRUCTURED
                        ? "[学习者事实] " : "[他说过] ") + item.text())
                .toList();
    }

    /**
     * 一轮成功结束：写入助手消息并执行存储裁剪。
     *
     * <p><b>正文为空但有思考</b>时不写空消息——调用方会传入一段可见提示（"思考 token 吃满了"这类），
     * 那才是用户该看到的内容。
     */
    @Transactional
    public void complete(long userId, long conversationId, String assistantContent, TokenUsage usage) {
        Instant now = Instant.now();
        conversations.findActiveByIdAndUser(conversationId, userId, now)
                .orElseThrow(ConversationService::conversationNotFound);

        appendMessage(conversationId, ConversationMessage.ROLE_ASSISTANT, assistantContent, toUsageJson(usage));
        conversations.slideExpiry(conversationId, userId, now.plus(properties.getTtl()), now);
    }

    private void appendMessage(long conversationId, String role, String content, String usageJson) {
        ConversationMessage message = new ConversationMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setTokenUsageJson(usageJson);
        messages.insert(message);

        long stored = messages.countByConversation(conversationId);
        int drop = ConversationWindow.messagesToDrop(stored, properties.getMaxStoredMessages());
        if (drop > 0) {
            messages.deleteOldest(conversationId, drop);
        }
    }

    /** 历史按窗口取最近 N 条，再翻成旧 → 新（模型要的顺序）。 */
    private List<ConversationTurn> history(long conversationId) {
        List<ConversationMessage> newestFirst =
                messages.selectRecentNewestFirst(conversationId, properties.getMaxContextMessages());
        List<ConversationMessage> oldestFirst = new ArrayList<>(newestFirst);
        java.util.Collections.reverse(oldestFirst);
        return oldestFirst.stream()
                .map(it -> new ConversationTurn(ConversationTurn.Role.of(it.getRole()), it.getContent()))
                .toList();
    }

    /** 条目上下文只取**已发布**的；草稿或已删条目当作"没有上下文"，不让别的模块读到草稿。 */
    private EntryContext entryContext(Long entryId) {
        if (entryId == null) {
            return EntryContext.NONE;
        }
        return knowledge.findPublishedContent(entryId)
                .map(it -> new EntryContext(it.title(), it.body()))
                .orElse(EntryContext.NONE);
    }

    private record EntryContext(String title, String body) {

        static final EntryContext NONE = new EntryContext(null, null);
    }

    /**
     * 对外一律这一条：不区分"不存在"与"不是你的"。
     *
     * <p>用量落库用简单 JSON（BYOK 下要返还给用户，随消息存一份就够，不需要再建表）。
     */
    private static String toUsageJson(TokenUsage usage) {
        if (usage == null) {
            return null;
        }
        return "{\"input\":" + usage.inputTokens()
                + ",\"output\":" + usage.outputTokens()
                + ",\"total\":" + usage.totalTokens() + "}";
    }

    private static BizException conversationNotFound() {
        return new BizException(ErrorCode.NOT_FOUND, "会话不存在或已过期");
    }
}
