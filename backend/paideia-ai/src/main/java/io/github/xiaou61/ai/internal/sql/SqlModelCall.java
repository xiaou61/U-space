package io.github.xiaou61.ai.internal.sql;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.DeepSeekFormatter;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.RunRecorder;
import io.github.xiaou61.ai.RunSpec;
import io.github.xiaou61.ai.internal.credential.CredentialService;
import io.github.xiaou61.ai.internal.provider.GuardedHttpTransport;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQL 相关的模型调用（WORK-035 T-12 / T-13）。
 *
 * <p><b>为什么抽一个共用类</b>：出题与造数据的调用骨架完全一样（凭据 → 记账 → 一次
 * 非流式调用 → 解析 JSON → 失败返回空），差别只在提示词与解析。复制两份会让
 * "记账漏了"或"超时没设"这类缺陷只在一处修。
 *
 * <p><b>失败不抛给调用方</b>：没有凭据、上游超时、JSON 坏掉都返回空 `Optional`——
 * 工作台仍然能手动写 SQL，不该因为 AI 不可用而整页报错。
 */
abstract class SqlModelCall {

    private static final Logger log = LoggerFactory.getLogger(SqlModelCall.class);

    /** 出题与造数据都可能要写较长的 SQL，给足上限。 */
    private static final int MAX_TOKENS = 8192;
    private static final double TEMPERATURE = 0.3;
    private static final Duration TIMEOUT = Duration.ofSeconds(90);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CredentialService credentials;
    private final GuardedHttpTransport transport;
    private final RunRecorder recorder;

    SqlModelCall(CredentialService credentials, GuardedHttpTransport transport, RunRecorder recorder) {
        this.credentials = credentials;
        this.transport = transport;
        this.recorder = recorder;
    }

    /** 这次调用的场景名（进 `ai_runs.kind`）。 */
    protected abstract String runKind();

    /** 提示词版本（进 `ai_runs.prompt_version`）。 */
    protected abstract String promptVersion();

    protected abstract String systemPrompt();

    protected abstract String userPrompt(Object request);

    /**
     * 跑一次调用并解析成 JSON。
     *
     * @return 解析后的根节点；任何一步失败都返回空
     */
    protected Optional<JsonNode> call(long userId, Object request) {
        var access = credentials.access(userId);
        if (access.isEmpty()) {
            log.info("{} 跳过：平台未配置 AI 凭据", runKind());
            return Optional.empty();
        }
        ModelAccess model = access.get();
        var run = recorder.begin(RunSpec.of(runKind(), userId)
                .withModel(model.model())
                .withPromptVersion(promptVersion()));
        try {
            GenerateOptions options = GenerateOptions.builder()
                    .modelName(model.model())
                    .stream(false)
                    .temperature(TEMPERATURE)
                    .maxTokens(MAX_TOKENS)
                    .build();
            OpenAIChatModel chat = OpenAIChatModel.builder()
                    .apiKey(model.apiKey())
                    .baseUrl(model.baseUrl())
                    .modelName(model.model())
                    .formatter(new DeepSeekFormatter())
                    .stream(false)
                    .generateOptions(options)
                    // 与 WORK-033 同一口径：DeepSeek 上工具调用与结构化输出不能同一次并用，
                    // 这里既不带工具也不用原生结构化输出
                    .nativeStructuredOutput(false)
                    .nativeStructuredOutputWithTools(false)
                    .httpTransport(transport)
                    .build();
            List<Msg> messages = List.of(
                    Msg.builder().role(MsgRole.SYSTEM).textContent(systemPrompt()).build(),
                    Msg.builder().role(MsgRole.USER).textContent(userPrompt(request)).build());
            ChatResponse response = chat.stream(messages, List.of(), options).blockLast(TIMEOUT);
            if (response == null || response.getContent() == null) {
                throw new IllegalStateException("上游没有返回内容");
            }
            StringBuilder text = new StringBuilder();
            for (var block : response.getContent()) {
                if (block instanceof TextBlock textBlock) {
                    text.append(textBlock.getText());
                }
            }
            if (response.getUsage() != null) {
                run.tokens(response.getUsage().getInputTokens(),
                        response.getUsage().getOutputTokens(), 0);
            }
            JsonNode parsed = objectMapper.readTree(extractJson(text.toString()));
            run.finish(io.github.xiaou61.ai.RunOutcome.OK, null);
            return Optional.of(parsed);
        } catch (RuntimeException | java.io.IOException exception) {
            log.warn("{} 失败：{}", runKind(), exception.getMessage());
            run.finish(io.github.xiaou61.ai.RunOutcome.FAILED, null);
            return Optional.empty();
        } finally {
            run.close();
        }
    }

    /**
     * 从模型输出里取出 JSON。
     *
     * <p>**必须容忍围栏**：即使提示词说了"只输出 JSON"，模型仍常写成
     * ` ```json ... ``` `。直接 `readTree` 会失败，而那种失败看起来像"模型没回答"。
     */
    static String extractJson(String text) {
        String trimmed = text.trim();
        int fence = trimmed.indexOf("```");
        if (fence >= 0) {
            int start = trimmed.indexOf('\n', fence);
            int end = trimmed.indexOf("```", start + 1);
            if (start > 0 && end > start) {
                return trimmed.substring(start + 1, end).trim();
            }
        }
        // 没有围栏时按第一个 { 到最后一个 } 取
        int open = trimmed.indexOf('{');
        int close = trimmed.lastIndexOf('}');
        if (open >= 0 && close > open) {
            return trimmed.substring(open, close + 1);
        }
        return trimmed;
    }
}
