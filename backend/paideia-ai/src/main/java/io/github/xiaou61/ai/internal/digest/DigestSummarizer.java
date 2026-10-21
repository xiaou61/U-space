package io.github.xiaou61.ai.internal.digest;

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
import io.github.xiaou61.ai.internal.provider.GuardedHttpTransport;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 每天那一次 AI 压缩（WORK-027）。
 *
 * <p>与 {@code EssayExtractor} 同一套最小面：一个 OpenAI 兼容模型 + DeepSeek formatter
 * + 一次非流式调用，没有 agent 运行时。
 *
 * <p><b>三条部署已知限制</b>（照抄既有结论，理由相同）：
 * <ul>
 *   <li>{@code nativeStructuredOutput(false)}：DeepSeek 不支持原生结构化输出；</li>
 *   <li>不用工具——"工具与结构化输出同一次调用"会互相干扰；</li>
 *   <li>思考 token 吃 {@code maxTokens}，给足 8192，吃满时上游会静默返回空。</li>
 * </ul>
 *
 * <p><b>失败不抛给调用方</b>：返回空 map，编排层把全部条目标 {@code degraded} 入库
 * （AC-005）。"标题 + 链接本身就有价值"。
 */
@Component
class DigestSummarizer {

    private static final Logger log = LoggerFactory.getLogger(DigestSummarizer.class);

    /** 一次要给几十条写标题与摘要，给足。 */
    private static final int MAX_TOKENS = 8192;

    /** 编辑类任务，要稳。 */
    private static final double TEMPERATURE = 0.3;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GuardedHttpTransport transport;

    DigestSummarizer(GuardedHttpTransport transport) {
        this.transport = transport;
    }

    /** 一条 AI 写出的结果。 */
    record Rewritten(String title, String summary) {
    }

    /**
     * 一次调用的产物：改写结果 + token 用量。
     *
     * <p>用量要带出来是为了进 `ai_runs`（AC-008）：日报一天只调一次，运营统计里看不到这条链路
     * 就等于"最贵的那次调用不在账上"。
     */
    record Outcome(Map<String, Rewritten> rewritten, io.github.xiaou61.ai.TokenUsage usage) {
    }

    /**
     * 调一次模型，返回 fingerprint → 改写结果。
     *
     * <p>**只接受输入集合内的指纹**：模型返回集合外的键直接丢弃，不写垃圾行。
     * 某个指纹没返回就等于"这条降级"，由调用方处理。
     *
     * <p>**不抛异常**：调用失败返回空 map + null 用量，由编排层把全部条目标 degraded
     * （AC-005）。
     */
    Outcome summarize(List<FetchedItem> items, List<String> fingerprints,
                      ModelAccess access, int timeoutSeconds) {
        if (items.isEmpty()) {
            return new Outcome(Map.of(), null);
        }
        try {
            CallResult result = call(items, fingerprints, access, timeoutSeconds);
            return new Outcome(parse(result.text(), fingerprints), result.usage());
        } catch (RuntimeException exception) {
            // 整批降级，不中断（AC-005 的单元级证据）
            log.warn("速递 AI 压缩失败，全部按降级入库：{}", exception.getMessage());
            return new Outcome(Map.of(), null);
        }
    }

    /** 一次上游调用的原始产物。 */
    private record CallResult(String text, io.github.xiaou61.ai.TokenUsage usage) {
    }

    private CallResult call(List<FetchedItem> items, List<String> fingerprints, ModelAccess access,
                            int timeoutSeconds) {
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
                Msg.builder().role(MsgRole.SYSTEM).textContent(DigestPrompt.system()).build(),
                Msg.builder().role(MsgRole.USER)
                        .textContent(DigestPrompt.user(items, fingerprints)).build());

        ChatResponse response = model.stream(messages, List.of(), options)
                .blockLast(Duration.ofSeconds(timeoutSeconds));
        if (response == null) {
            throw new IllegalStateException("上游没有返回内容");
        }
        StringBuilder text = new StringBuilder();
        if (response.getContent() != null) {
            for (var block : response.getContent()) {
                if (block instanceof TextBlock textBlock) {
                    text.append(textBlock.getText());
                }
            }
        }
        var usage = response.getUsage() == null ? null
                : new io.github.xiaou61.ai.TokenUsage(response.getUsage().getInputTokens(),
                        response.getUsage().getOutputTokens(), response.getUsage().getTotalTokens());
        return new CallResult(text.toString(), usage);
    }

    /**
     * 解析模型输出。
     *
     * <p>容忍两种情况：整体被 markdown 围栏包住、或前后有解释文字——都靠"取第一个 `{`
     * 到最后一个 `}`"兜住。**空内容按失败处理**：DeepSeek 会把 maxTokens 吃在思考上
     * 并静默返回空，那不是"今天没什么可写"。
     */
    Map<String, Rewritten> parse(String raw, List<String> allowedFingerprints) {
        if (raw == null || raw.isBlank()) {
            log.warn("速递 AI 返回空内容（思考 token 可能吃满），全部按降级处理");
            return Map.of();
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            log.warn("速递 AI 返回里没有 JSON 对象，全部按降级处理");
            return Map.of();
        }
        try {
            JsonNode root = objectMapper.readTree(raw.substring(start, end + 1));
            JsonNode list = root.path("items");
            if (!list.isArray()) {
                return Map.of();
            }
            java.util.Set<String> allowed = new java.util.HashSet<>(allowedFingerprints);
            Map<String, Rewritten> result = new LinkedHashMap<>();
            for (JsonNode node : list) {
                String fingerprint = node.path("fingerprint").asText("");
                // **集合外的指纹直接丢**：模型编的键不能进库
                if (!allowed.contains(fingerprint)) {
                    continue;
                }
                String title = node.path("title").asText("");
                String summary = node.path("summary").asText("");
                if (title.isBlank() && summary.isBlank()) {
                    continue;
                }
                result.put(fingerprint, new Rewritten(title, summary));
            }
            return result;
        } catch (java.io.IOException exception) {
            log.warn("速递 AI 返回的 JSON 解析失败：{}", exception.getClass().getSimpleName());
            return Map.of();
        }
    }
}
