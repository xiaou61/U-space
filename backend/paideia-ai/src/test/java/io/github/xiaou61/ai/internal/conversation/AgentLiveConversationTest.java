package io.github.xiaou61.ai.internal.conversation;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xiaou61.ai.AssistantConversation;
import io.github.xiaou61.ai.Harness;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.NoteDraft;
import io.github.xiaou61.ai.RunKind;
import io.github.xiaou61.ai.RunSpec;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.internal.provider.GuardedHttpTransport;
import io.github.xiaou61.ai.internal.provider.UpstreamGuard;
import io.github.xiaou61.ai.internal.telemetry.NoopRunRecorder;
import io.github.xiaou61.ai.internal.tools.AgentToolBridge;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.PaideiaTool;
import io.github.xiaou61.platform.ToolCatalog;
import io.github.xiaou61.platform.ToolContext;
import io.github.xiaou61.platform.ToolDescriptor;
import io.github.xiaou61.platform.ToolResult;
import io.github.xiaou61.platform.ToolScope;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * 真连 DeepSeek 跑一遍完整对话链路（WORK-031 第二步）。
 *
 * <p>**默认跳过**：需要真实凭据与网络。要跑就这样：
 * <pre>
 * mvn -pl paideia-ai test -Dtest=AgentLiveConversationTest \
 *     -Dpaideia.ai.live=true \
 *     -Dpaideia.ai.live.api-key=... -Dpaideia.ai.live.base-url=https://api.deepseek.com
 * </pre>
 *
 * <p><b>为什么必须有一条真连的</b>：单测里的模型是假的，它能证明"我们的循环与中间件配合正确"，
 * 但证明不了"DeepSeek 会照着这套协议返回"。三处实测补丁（旁白重复、tool 消息配对、思考不进历史）
 * 当初就是只有在真上游上才暴露的。
 */
@EnabledIfSystemProperty(named = "paideia.ai.live", matches = "true")
class AgentLiveConversationTest {

    private static ModelAccess access() {
        return new ModelAccess(
                System.getProperty("paideia.ai.live.base-url", "https://api.deepseek.com"),
                System.getProperty("paideia.ai.live.model", "deepseek-flash"),
                System.getProperty("paideia.ai.live.api-key", ""));
    }

    /** 只记录回调。 */
    private static final class Recorder implements AssistantConversation.Listener {
        final StringBuilder content = new StringBuilder();
        final StringBuilder thinking = new StringBuilder();
        final List<NoteDraft> drafts = new ArrayList<>();
        final List<TokenUsage> usages = new ArrayList<>();
        boolean completed;
        BizException failed;

        @Override public void onThinking(String delta) { thinking.append(delta); }
        @Override public void onContent(String delta) { content.append(delta); }
        @Override public void onDraft(NoteDraft draft) { drafts.add(draft); }
        @Override public void onUsage(TokenUsage usage) { usages.add(usage); }
        @Override public void onCompleted() { completed = true; }
        @Override public void onFailed(BizException failure) { failed = failure; }
    }

    private static Recorder stream(AssistantConversation conversation, String userMessage) {
        Recorder recorder = new Recorder();
        conversation.stream(
                new AssistantConversation.Request(1L, List.of(), userMessage, null, null, "", List.of(),
                        Harness.ASSISTANT),
                access(),
                new NoopRunRecorder().begin(RunSpec.of(RunKind.CHAT, 1L)),
                recorder);
        return recorder;
    }

    @Test
    void liveConversationProducesAnswerAndUsage() {
        AgentScopeConversation conversation = new AgentScopeConversation(
                properties(), new GuardedHttpTransport(new UpstreamGuard()), bridge());

        Recorder recorder = stream(conversation, "用一句话说清 Redis 是什么。");

        assertThat(recorder.failed).as("真实上游不该失败：%s", recorder.failed).isNull();
        assertThat(recorder.completed).isTrue();
        assertThat(recorder.content.toString()).as("必须真的有正文").isNotBlank();
        assertThat(recorder.usages).hasSize(1);
        assertThat(recorder.usages.get(0)).as("用量要有 input/output").isNotNull();
        assertThat(recorder.usages.get(0).inputTokens()).isPositive();
    }

    @Test
    void liveToolLoopExecutesTool() {
        AgentScopeConversation conversation = new AgentScopeConversation(
                properties(), new GuardedHttpTransport(new UpstreamGuard()), bridge());

        // 这句只有调用工具才能答对：光靠模型知识答不出我们的课程清单
        Recorder recorder = stream(conversation,
                "平台现在开了哪几门课？必须先调用 knowledge_course_catalog 工具查，不要凭猜。");

        assertThat(recorder.failed).as("真实上游不该失败：%s", recorder.failed).isNull();
        assertThat(recorder.completed).isTrue();
        assertThat(recorder.content.toString()).as("工具轮之后必须有最终答案").isNotBlank();
        assertThat(recorder.content.toString())
                .as("答案必须来自工具返回的内容，证明工具真的被执行且结果被回喂")
                .contains("Redis");
        // 工具轮 + 出答案轮 = 至少两次上游调用，用量是各轮之和
        assertThat(recorder.usages).hasSize(1);
        assertThat(recorder.usages.get(0).totalTokens()).isPositive();
    }

    private static ConversationProperties properties() {
        ConversationProperties properties = new ConversationProperties();
        properties.setRequestTimeoutSeconds(120);
        return properties;
    }

    /** 带一个真工具：验证工具循环在真实上游上真的跑通（不只是"单轮纯文本"）。 */
    private static AgentToolBridge bridge() {
        return new AgentToolBridge(new ToolCatalog(List.of(new CourseCatalogTool())));
    }

    /** 一个最小的平台工具：返回固定的课程清单，证明工具循环能执行并回喂结果。 */
    private static final class CourseCatalogTool implements PaideiaTool {

        @Override
        public ToolDescriptor descriptor() {
            return new ToolDescriptor(
                    "knowledge_course_catalog", "knowledge", "课程清单",
                    "查询平台当前开放的课程名称列表。当用户问「有哪些课」时必须调用它。",
                    Map.of("type", "object",
                            "properties", Map.of("keyword", Map.of("type", "string",
                                    "description", "可选：按关键词过滤")),
                            "required", List.of()),
                    null, true, false, true, ToolScope.READ, true);
        }

        @Override
        public ToolResult call(ToolContext context, Map<String, Object> arguments) {
            return ToolResult.ok("平台目前开放 3 门课：Redis 入门、MySQL 索引、JVM 内存模型。");
        }
    }
}
