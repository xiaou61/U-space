package io.github.xiaou61.ai.internal.conversation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ThinkingBlock;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.ChatUsage;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.github.xiaou61.ai.AssistantConversation;
import io.github.xiaou61.ai.Harness;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.NoteDraft;
import io.github.xiaou61.ai.RunSpec;
import io.github.xiaou61.ai.RunKind;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.internal.telemetry.NoopRunRecorder;
import io.github.xiaou61.ai.internal.tools.AgentToolBridge;
import io.github.xiaou61.platform.BizException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

/**
 * ReActAgent 迁移后必须保住的四条行为（WORK-031 第二步）。
 *
 * <p><b>为什么必须有一个类专门钉住它们</b>：这三条都是 2026-10-19 用手写循环"实测算出来"的补丁
 * （旁白重复、tool 消息配对、思考混正文）。换成框架驱动后，框架的行为不保证与原补丁一致，
 * 而既有集成测试把端口整个桩掉了——桩里根本没有循环，看不见这些。
 *
 * <p>用假模型走**真实的 ReActAgent**：这样测的是框架 + 我们中间件的组合，而不是我们的假设。
 */
class AgentTurnBehaviorTest {

    private static ChatResponse text(String content, int in, int out) {
        return new ChatResponse("r", List.of(TextBlock.builder().text(content).build()),
                new ChatUsage(in, out, 0.0), Map.of(), "stop");
    }

    private static ChatResponse thinkingThenText(String thinking, String content) {
        List<ContentBlock> blocks = new ArrayList<>();
        blocks.add(ThinkingBlock.builder().thinking(thinking).build());
        blocks.add(TextBlock.builder().text(content).build());
        return new ChatResponse("r", blocks, new ChatUsage(10, 5, 0.0), Map.of(), "stop");
    }

    private static ChatResponse toolCall(String name) {
        ContentBlock call = ToolUseBlock.builder().id("c-" + name).name(name).input(Map.of()).build();
        return new ChatResponse("r", List.of(call), new ChatUsage(10, 5, 0.0), Map.of(), "tool_calls");
    }

    /** 按脚本逐轮吐响应；轮次用完就重复最后一轮。 */
    private static final class ScriptedModel implements Model {
        private final List<List<ChatResponse>> rounds;
        private int index;
        private final List<List<String>> seenToolNames = new ArrayList<>();

        ScriptedModel(List<List<ChatResponse>> rounds) {
            this.rounds = rounds;
        }

        @Override
        public Flux<ChatResponse> stream(List<Msg> messages,
                                         List<io.agentscope.core.model.ToolSchema> schemas,
                                         GenerateOptions options) {
            seenToolNames.add(schemas.stream().map(io.agentscope.core.model.ToolSchema::getName).toList());
            List<ChatResponse> picked = rounds.get(Math.min(index, rounds.size() - 1));
            index++;
            return Flux.fromIterable(picked);
        }

        @Override
        public String getModelName() {
            return "scripted";
        }
    }

    private static final class ScriptedConversation extends AgentScopeConversation {
        private final ScriptedModel model;

        ScriptedConversation(ScriptedModel model, AgentToolBridge tools) {
            super(new ConversationProperties(), null, tools);
            this.model = model;
        }

        @Override
        Model buildModel(ModelAccess access, GenerateOptions options) {
            return model;
        }
    }

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

    /** 默认桩：没有核心工具、没有发现元工具。需要工具的用例自己另建桩。 */
    private static AgentToolBridge toolsStub() {
        AgentToolBridge tools = mock(AgentToolBridge.class);
        when(tools.core(any())).thenReturn(List.of());
        when(tools.callable(any())).thenReturn(List.of());
        when(tools.discoverySchemas()).thenReturn(List.of());
        when(tools.concat(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tools.run(anyLong(), any(), anyString(), any()))
                .thenReturn(new AgentToolBridge.Outcome(false, "工具结果", null, List.of()));
        return tools;
    }

    /**
     * 带草稿工具的桩。
     *
     * <p>`core` 与 `callable` 必须**都给**：`core` 决定这一轮发给模型看什么，
     * `callable` 决定执行侧注册什么。只给前者时模型会发出一个执行不到的调用（实测约束）。
     */
    private static AgentToolBridge draftToolsStub() {
        AgentToolBridge tools = mock(AgentToolBridge.class);
        var draftSchema = io.agentscope.core.model.ToolSchema.builder()
                .name("assistant_draft_note")
                .description("起草一条笔记")
                .parameters(Map.of("type", "object"))
                .build();
        when(tools.core(any())).thenReturn(List.of(draftSchema));
        when(tools.callable(any())).thenReturn(List.of(draftSchema));
        when(tools.discoverySchemas()).thenReturn(List.of());
        when(tools.concat(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tools.run(anyLong(), any(), anyString(), any()))
                .thenReturn(new AgentToolBridge.Outcome(false, "已准备草稿", new NoteDraft("标题", "正文"), List.of()));
        return tools;
    }

    private static Recorder run(AssistantConversation conversation, Harness harness) {
        Recorder recorder = new Recorder();
        conversation.stream(
                new AssistantConversation.Request(1L, List.of(), "在吗", null, null, "", List.of(), harness),
                new ModelAccess("http://localhost", "scripted", "k"),
                new NoopRunRecorder().begin(RunSpec.of(RunKind.CHAT, 1L)),
                recorder);
        return recorder;
    }

    @Test
    @DisplayName("工具轮的旁白不推给用户：只有没有工具调用的那一轮才算回答")
    void toolRoundNarrationIsNotEmitted() {
        ScriptedModel model = new ScriptedModel(List.of(
                List.of(toolCall("search_entries")),
                List.of(text("最终答案", 30, 10))));

        Recorder recorder = run(new ScriptedConversation(model, toolsStub()), Harness.ASSISTANT);

        assertThat(recorder.content.toString())
                .as("只应有最终答案，不含工具轮的旁白")
                .isEqualTo("最终答案");
        assertThat(recorder.completed).isTrue();
    }

    @Test
    @DisplayName("思考不进正文，且通过单独的通道送达")
    void thinkingStaysOutOfContent() {
        ScriptedModel model = new ScriptedModel(List.of(
                List.of(thinkingThenText("我在想……", "答案"))));

        Recorder recorder = run(new ScriptedConversation(model, toolsStub()), Harness.ASSISTANT);

        assertThat(recorder.content.toString()).isEqualTo("答案");
        assertThat(recorder.thinking.toString())
                .as("思考走单独通道，不混进正文")
                .contains("我在想");
        assertThat(recorder.content.toString()).doesNotContain("我在想");
    }

    @Test
    @DisplayName("用量是各轮之和（WORK-009 T-01 不许倒退）")
    void usageSumsAcrossRounds() {
        ScriptedModel model = new ScriptedModel(List.of(
                List.of(toolCall("search_entries")),
                List.of(text("答", 30, 10))));

        Recorder recorder = run(new ScriptedConversation(model, toolsStub()), Harness.ASSISTANT);

        assertThat(recorder.usages).containsExactly(new TokenUsage(40, 15, 55, 0));
    }

    @Test
    @DisplayName("多轮工具调用后仍能正常出答案（不因 tool 消息配对而 400）")
    void multipleToolRoundsStillAnswer() {
        ScriptedModel model = new ScriptedModel(List.of(
                List.of(toolCall("search_entries")),
                List.of(toolCall("search_entries")),
                List.of(text("够了", 5, 5))));

        Recorder recorder = run(new ScriptedConversation(model, toolsStub()), Harness.ASSISTANT);

        assertThat(recorder.content.toString()).isEqualTo("够了");
        assertThat(recorder.completed).isTrue();
    }

    @Test
    @DisplayName("草稿在流中途送到回调，不混进正文")
    void draftIsDeliveredSeparately() {
        AgentToolBridge tools = draftToolsStub();

        ScriptedModel model = new ScriptedModel(List.of(
                List.of(toolCall("assistant_draft_note")),
                List.of(text("草稿给你了", 5, 5))));

        Recorder recorder = run(new ScriptedConversation(model, tools), Harness.ASSISTANT);

        assertThat(recorder.drafts).hasSize(1);
        assertThat(recorder.drafts.get(0).body()).isEqualTo("正文");
        assertThat(recorder.content.toString()).isEqualTo("草稿给你了");
    }

    @Test
    @DisplayName("find_tools 发现的工具在下一轮才可见，且执行侧已注册（WORK-031 的核心约束）")
    void discoveredToolBecomesVisibleNextRound() {
        // discover 工具一开始可见；secret 工具在"发现前"不可见，但执行侧已注册
        var discoverSchema = io.agentscope.core.model.ToolSchema.builder()
                .name("find_tools")
                .description("搜工具")
                .parameters(Map.of("type", "object"))
                .build();
        var secretSchema = io.agentscope.core.model.ToolSchema.builder()
                .name("secret_tool")
                .description("只有被发现后才可见")
                .parameters(Map.of("type", "object"))
                .build();

        AgentToolBridge tools = mock(AgentToolBridge.class);
        when(tools.core(any())).thenReturn(List.of(discoverSchema));
        // 执行侧把 secret 也注册上——这正是"运行中注册调不动"的绕法
        when(tools.callable(any())).thenReturn(List.of(discoverSchema, secretSchema));
        when(tools.discoverySchemas()).thenReturn(List.of(discoverSchema));
        when(tools.concat(any(), any())).thenAnswer(invocation -> {
            List<io.agentscope.core.model.ToolSchema> core = invocation.getArgument(0);
            java.util.Collection<String> discovered = invocation.getArgument(1);
            List<io.agentscope.core.model.ToolSchema> merged = new ArrayList<>(core);
            if (discovered.contains("secret_tool")) {
                merged.add(secretSchema);
            }
            return merged;
        });
        when(tools.run(anyLong(), any(), anyString(), any())).thenAnswer(invocation -> {
            String name = invocation.getArgument(2);
            if ("find_tools".equals(name)) {
                return new AgentToolBridge.Outcome(false, "找到 secret_tool",
                        null, List.of("secret_tool"));
            }
            return new AgentToolBridge.Outcome(false, "秘密结果", null, List.of());
        });

        ScriptedModel model = new ScriptedModel(List.of(
                List.of(toolCall("find_tools")),
                List.of(toolCall("secret_tool")),
                List.of(text("拿到了", 5, 5))));

        Recorder recorder = run(new ScriptedConversation(model, tools), Harness.ASSISTANT);

        // 第 1 轮：只有 discover + 发现元工具（去重后仍只有 discover）
        assertThat(model.seenToolNames.get(0))
                .as("发现之前看不见 secret_tool")
                .doesNotContain("secret_tool");
        // 第 2 轮：secret 已经出现在发给模型的清单里
        assertThat(model.seenToolNames.get(1))
                .as("发现之后下一轮立即可见")
                .contains("secret_tool");
        assertThat(recorder.content.toString()).isEqualTo("拿到了");
        assertThat(recorder.completed).isTrue();
    }

    @Test
    @DisplayName("点评 harness 不带任何工具（DeepSeek 工具与结构化输出不能并用的约束）")
    void reviewHarnessSeesNoTools() {
        ScriptedModel model = new ScriptedModel(List.of(List.of(text("评分", 5, 5))));

        run(new ScriptedConversation(model, toolsStub()), Harness.REVIEW);

        assertThat(model.seenToolNames)
                .as("点评那一轮发给模型的工具清单必须为空")
                .containsExactly(List.of());
    }
}
