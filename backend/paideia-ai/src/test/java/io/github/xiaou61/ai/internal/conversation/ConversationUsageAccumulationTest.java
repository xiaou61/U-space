package io.github.xiaou61.ai.internal.conversation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolUseBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.ChatUsage;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.ToolSchema;
import io.github.xiaou61.ai.AssistantConversation;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.NoteDraft;
import io.github.xiaou61.ai.TokenUsage;
import io.github.xiaou61.ai.Harness;
import io.github.xiaou61.ai.internal.tools.AgentToolBridge;
import io.github.xiaou61.platform.BizException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

/**
 * 工具循环里的用量必须**逐轮累加**（WORK-009 T-01）。
 *
 * <p><b>为什么单独一个类、而且要在"模型"这一层做假</b>：这个缺陷只存在于"一次对话里发生了多轮上游
 * 调用"这条路径上。而既有的会话集成测试把**整个端口**桩掉了——桩里根本没有循环，所以它永远看不见
 * 这个 bug。要看见它，只能把假模型塞进工具循环里面。
 *
 * <p><b>反向验证</b>：把 {@code stream} 里那句 {@code total = add(total, roundResult.usage())} 删掉
 * （退回"只报最后一轮"），本类三个用例全红。
 */
class ConversationUsageAccumulationTest {

    /** 一轮的响应：正文 + 用量。 */
    private static ChatResponse textRound(String text, int input, int output) {
        return new ChatResponse("r-" + text, List.of(TextBlock.builder().text(text).build()),
                new ChatUsage(input, output, 0.0), Map.of(), "stop");
    }

    /** 一轮的响应：要求调工具 + 用量。 */
    private static ChatResponse toolRound(String toolName, int input, int output) {
        ContentBlock call = ToolUseBlock.builder().id("c-" + toolName).name(toolName).input(Map.of()).build();
        return new ChatResponse("r-" + toolName, List.of(call),
                new ChatUsage(input, output, 0.0), Map.of(), "tool_calls");
    }

    /** 按脚本逐轮吐响应的模型；轮次用完就一直重复最后一轮。 */
    private static final class ScriptedModel implements Model {

        private final List<List<ChatResponse>> rounds;
        private int index;

        ScriptedModel(List<List<ChatResponse>> rounds) {
            this.rounds = rounds;
        }

        int calls() {
            return index;
        }

        @Override
        public Flux<ChatResponse> stream(List<Msg> messages, List<ToolSchema> schemas, GenerateOptions options) {
            List<ChatResponse> picked = rounds.get(Math.min(index, rounds.size() - 1));
            index++;
            return Flux.fromIterable(picked);
        }

        @Override
        public String getModelName() {
            return "scripted";
        }
    }

    /** 把模型换成脚本的会话实现。transport 传 null：buildModel 被覆写，它不会被用到。 */
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

    /** 只记录回调，不做任何 I/O。 */
    private static final class Recorder implements AssistantConversation.Listener {

        final List<TokenUsage> usages = new ArrayList<>();
        boolean completed;
        BizException failed;

        @Override
        public void onThinking(String delta) {
        }

        @Override
        public void onContent(String delta) {
        }

        @Override
        public void onDraft(NoteDraft draft) {
        }

        @Override
        public void onUsage(TokenUsage usage) {
            usages.add(usage);
        }

        @Override
        public void onCompleted() {
            completed = true;
        }

        @Override
        public void onFailed(BizException failure) {
            failed = failure;
        }
    }

    private static AgentToolBridge toolsStub() {
        AgentToolBridge tools = mock(AgentToolBridge.class);
        when(tools.core(any())).thenReturn(List.of());
        when(tools.discoverySchemas()).thenReturn(List.of());
        when(tools.concat(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(tools.run(anyLong(), any(), anyString(), any()))
                .thenReturn(new AgentToolBridge.Outcome(false, "工具结果", null, List.of()));
        return tools;
    }

    private static Recorder run(ScriptedConversation conversation) {
        Recorder recorder = new Recorder();
        conversation.stream(
                new AssistantConversation.Request(1L, List.of(), "在吗", null, null, List.of()),
                new ModelAccess("http://localhost", "scripted", "k"),
                new io.github.xiaou61.ai.internal.telemetry.NoopRunRecorder().begin(
                        io.github.xiaou61.ai.RunSpec.of(io.github.xiaou61.ai.RunKind.CHAT, 1L)),
                recorder);
        return recorder;
    }

    @Test
    @DisplayName("两轮（一轮要工具 + 一轮出正文）：用量是两轮之和，不是最后一轮")
    void sumsUsageAcrossToolRounds() {
        ScriptedModel model = new ScriptedModel(List.of(
                List.of(toolRound("search_entries", 100, 20)),
                List.of(textRound("答案", 30, 10))));
        Recorder recorder = run(new ScriptedConversation(model, toolsStub()));

        assertThat(model.calls()).as("确实发生了两轮上游调用").isEqualTo(2);
        assertThat(recorder.usages).containsExactly(new TokenUsage(130, 30, 160));
        assertThat(recorder.completed).isTrue();
        assertThat(recorder.failed).isNull();
    }

    @Test
    @DisplayName("绕满轮次放弃：也要把已发生的用量上报（这一条原先一笔都不记）")
    void reportsUsageEvenWhenTheToolLoopGivesUp() {
        // 每轮都要求调工具 → 循环跑满 MAX_TOOL_ROUNDS 后放弃
        ScriptedModel model = new ScriptedModel(List.of(List.of(toolRound("search_entries", 10, 5))));
        Recorder recorder = run(new ScriptedConversation(model, toolsStub()));

        int rounds = model.calls();
        assertThat(rounds).as("绕满轮次").isGreaterThan(1);
        assertThat(recorder.usages).containsExactly(new TokenUsage(10 * rounds, 5 * rounds, 15 * rounds));
        assertThat(recorder.failed).as("放弃时走失败回调").isNotNull();
        assertThat(recorder.completed).isFalse();
    }

    @Test
    @DisplayName("上游整轮没吐东西：用量按已发生的算，失败语义不变")
    void reportsUsageWhenUpstreamReturnsNothing() {
        ScriptedModel model = new ScriptedModel(List.of(List.of()));
        Recorder recorder = run(new ScriptedConversation(model, toolsStub()));

        assertThat(recorder.usages).containsExactly((TokenUsage) null);
        assertThat(recorder.failed).isNotNull();
    }

    @Test
    @DisplayName("add：任一段缺失取另一段，两段都缺才是 null")
    void addIsNullSafe() {
        TokenUsage six = new TokenUsage(1, 2, 3);
        assertThat(AgentScopeConversation.add(null, six)).isSameAs(six);
        assertThat(AgentScopeConversation.add(six, null)).isSameAs(six);
        assertThat(AgentScopeConversation.add(null, null)).isNull();
        assertThat(AgentScopeConversation.add(six, six)).isEqualTo(new TokenUsage(2, 4, 6));
    }
}
