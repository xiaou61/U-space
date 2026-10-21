package io.github.xiaou61.ai.internal.digest;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.GenerateOptions;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.DeepSeekFormatter;
import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.internal.credential.CredentialService;
import io.github.xiaou61.ai.internal.provider.GuardedHttpTransport;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 周报：从上一周的日报聚合，一次 AI 调用写「本周技术主线」（WORK-027）。
 *
 * <p><b>独立调度、独立 try/catch</b>：周报失败不影响日报（AC-013）。
 *
 * <p><b>同周只留一条</b>：`week_start` 唯一，重跑用 upsert 覆盖（AC-016）。
 */
@Service
public class DigestWeeklyService {

    private static final Logger log = LoggerFactory.getLogger(DigestWeeklyService.class);

    private static final int MAX_TOKENS = 4096;
    private static final double TEMPERATURE = 0.4;

    private final DigestProperties properties;
    private final DigestMapper mapper;
    private final CredentialService credentials;
    private final GuardedHttpTransport transport;
    private final io.github.xiaou61.ai.RunRecorder recorder;

    DigestWeeklyService(DigestProperties properties, DigestMapper mapper,
                        CredentialService credentials, GuardedHttpTransport transport,
                        io.github.xiaou61.ai.RunRecorder recorder) {
        this.properties = properties;
        this.mapper = mapper;
        this.credentials = credentials;
        this.transport = transport;
        this.recorder = recorder;
    }

    /** 一次周报运行的结果。 */
    public record WeeklyResult(LocalDate weekStart, int itemCount, boolean created, String skipReason) {

        static WeeklyResult skipped(LocalDate weekStart, String reason) {
            return new WeeklyResult(weekStart, 0, false, reason);
        }
    }

    /**
     * 生成本周周报。
     *
     * <p>**任何失败都返回带原因的跳过，不抛异常**：周报是锦上添花，不能连累日报。
     */
    public WeeklyResult runWeekly() {
        if (!properties.isEnabled()) {
            return WeeklyResult.skipped(lastWeekStart(), "未启用");
        }
        LocalDate weekStart = lastWeekStart();
        try {
            return generate(weekStart);
        } catch (RuntimeException exception) {
            log.warn("周报生成失败（{}），日报不受影响：{}", weekStart, exception.getMessage());
            return WeeklyResult.skipped(weekStart, "生成失败：" + exception.getMessage());
        }
    }

    private WeeklyResult generate(LocalDate weekStart) {
        // 素材就是这一周（周一到周日）的日报
        LocalDate from = weekStart;
        LocalDate to = weekStart.plusDays(6);
        List<Map<String, Object>> titles =
                mapper.titlesBetween(from, to, properties.getWeeklyMaxItems());
        if (titles.isEmpty()) {
            return WeeklyResult.skipped(weekStart, "上周没有速递内容");
        }
        Optional<ModelAccess> access = credentials.access(0);
        if (access.isEmpty()) {
            return WeeklyResult.skipped(weekStart, "平台未配置 AI 凭据");
        }

        // 周报单独一条 ai_runs 记录，便于与日报分开看成本（AC-008 的延伸）。
        // userId=0 表示平台行为（速递没有用户归属）。
        var run = recorder.begin(io.github.xiaou61.ai.RunSpec
                .of(io.github.xiaou61.ai.RunKind.DIGEST_WEEKLY, 0L)
                .withModel(access.get().model()));
        String body;
        try {
            body = callModel(titles, access.get());
            if (body != null && !body.isBlank()) {
                run.finish(io.github.xiaou61.ai.RunOutcome.OK, null);
            } else {
                run.finish(io.github.xiaou61.ai.RunOutcome.FAILED, "empty_response");
            }
        } finally {
            run.close();
        }
        if (body == null || body.isBlank()) {
            return WeeklyResult.skipped(weekStart, "模型没有返回内容");
        }
        mapper.upsertWeekly(weekStart, body, titles.size(), Instant.now());
        log.info("周报已生成：week={}，素材 {} 条", weekStart, titles.size());
        return new WeeklyResult(weekStart, titles.size(), true, null);
    }

    private String callModel(List<Map<String, Object>> titles, ModelAccess access) {
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
                Msg.builder().role(MsgRole.SYSTEM).textContent(SYSTEM).build(),
                Msg.builder().role(MsgRole.USER).textContent(user(titles)).build());
        ChatResponse response = model.stream(messages, List.of(), options)
                .blockLast(Duration.ofSeconds(properties.getAiTimeoutSeconds()));
        if (response == null || response.getContent() == null) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        for (var block : response.getContent()) {
            if (block instanceof TextBlock textBlock) {
                text.append(textBlock.getText());
            }
        }
        return text.toString();
    }

    private static final String SYSTEM = """
            你是中文技术编辑。根据下面这一周的资讯标题，写一段「本周技术主线」。

            规则：
            1. 300~500 字，markdown，用 2~3 个小标题分段。
            2. **只根据给定标题写**，不编造没有的信息。
            3. 讲趋势与关联，不要逐条罗列。
            4. 直接输出正文，不要开场白。
            """;

    private static String user(List<Map<String, Object>> titles) {
        StringBuilder builder = new StringBuilder("本周的资讯标题：\n\n");
        for (Map<String, Object> row : titles) {
            builder.append("- [").append(row.get("group_key")).append("] ")
                    .append(row.get("title")).append('\n');
        }
        return builder.toString();
    }

    /**
     * **上一周**的周一（UTC）。
     *
     * <p>周报在周一凌晨跑、讲的是刚过去的那一周。若用"本周一"，素材会指向还没发生的那一周，
     * 生成出来的报告归属也会差一周。
     */
    public static LocalDate lastWeekStart() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .minusWeeks(1);
    }
}
