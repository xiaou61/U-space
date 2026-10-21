package io.github.xiaou61.ai.internal.telemetry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xiaou61.platform.PageQuery;
import io.github.xiaou61.platform.PageResult;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 管理端统计服务（WORK-022 T10）。
 *
 * <p><b>时间窗归一化在这里做一次</b>：缺省 7 天、上限 `retention-days`（默认 90）。
 * 不设上限的统计接口等于给了一张全表扫描的入口；收敛时明确返回 `clamped`，
 * 让"我怎么只看到 90 天"这件事有个可读的答案。
 *
 * <p>响应里**永远没有正文、提示词、工具参数**。这不是"注意一下"，是 AC-012 的断言对象。
 */
@Service
public class AiRunAdminService {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final AiRunMapper runs;
    private final AiRunSpanMapper spans;
    private final AiRunPayloadMapper payloads;
    private final TelemetrySummaryMapper summary;
    private final TelemetryProperties properties;

    AiRunAdminService(AiRunMapper runs, AiRunSpanMapper spans, AiRunPayloadMapper payloads,
                      TelemetrySummaryMapper summary, TelemetryProperties properties) {
        this.runs = runs;
        this.spans = spans;
        this.payloads = payloads;
        this.summary = summary;
        this.properties = properties;
    }

    /** 归一化后的时间窗。 */
    public record Window(Instant from, Instant to, boolean clamped) {
    }

    public record Overview(Instant from, Instant to, boolean clamped,
                    long total, StatusCounts byStatus,
                    double successRate,
                    Integer durationP50, Integer durationP95,
                    Integer ttftP50, Integer ttftP95,
                    long inputTokens, long outputTokens, long credits,
                    List<TelemetrySummaryMapper.CountBy> byKind,
                    List<TelemetrySummaryMapper.CountBy> byModel,
                    List<TelemetrySummaryMapper.CountBy> errorTypes,
                    List<TelemetrySummaryMapper.CountBy> tools) {
    }

    /**
     * 按状态的计数。**用固定字段而不是 Map**：契约快照按 JSON 形状比对，
     * 而 Map 的键取决于这一轮出现过哪些状态——同一段代码在不同数据上会得到不同形状，
     * 那种"快照漂移"不是缺陷，是形状没定死（2026-10-19 实测踩到）。
     */
    public record StatusCounts(long ok, long failed, long timeout, long cancelled, long interrupted, long running) {
    }

    public record RunSummary(String traceId, long userId, Long conversationId, String kind, String harness,
                      String model, String status, String errorType,
                      Instant startedAt, Instant endedAt,
                      Integer durationMs, Integer ttftMs, int llmRounds, int toolCalls,
                      int inputTokens, int outputTokens, int credits) {
    }

    public record SpanView(int seq, Integer parentSeq, String kind, String name, String status,
                    String errorType, Instant startedAt, Integer durationMs, Integer ttftMs,
                    int inputTokens, int outputTokens, Map<String, Object> attrs) {
    }

    /**
     * 一轮的详情。
     *
     * @param payloads 各 LLM 步的完整请求与响应（WORK-030 阶段 B）。**按 seq 对应步骤**，
     *                 前端展开某一步时按 seq 取。未开启采集时为空，`payloadEnabled` 告诉页面该说什么。
     */
    public record RunDetail(RunSummary run, Map<String, Object> attrs, List<SpanView> spans,
                            List<PayloadView> payloads, boolean payloadEnabled) {
    }

    /**
     * 一份完整载荷。
     *
     * @param truncated 内容超上限（>1MB），正文为空但字节数与标记都在
     */
    public record PayloadView(int seq, String requestBody, String responseBody,
                       int requestBytes, int responseBytes, boolean truncated) {
    }

    /** 时间窗：缺省 7 天；跨度超过保留期时收敛到保留期。 */
    public Window window(Instant from, Instant to) {
        Instant end = to == null ? Instant.now() : to;
        Instant start = from == null ? end.minus(7, ChronoUnit.DAYS) : from;
        Instant floor = end.minus(properties.getRetentionDays(), ChronoUnit.DAYS);
        boolean clamped = start.isBefore(floor);
        return new Window(clamped ? floor : start, end, clamped);
    }

    public Overview overview(Instant from, Instant to) {
        Window window = window(from, to);
        List<TelemetrySummaryMapper.CountBy> statuses = summary.countByStatus(window.from(), window.to());
        long ok = 0;
        long failed = 0;
        long timeout = 0;
        long cancelled = 0;
        long interrupted = 0;
        long running = 0;
        for (TelemetrySummaryMapper.CountBy row : statuses) {
            switch (row.name()) {
                case "ok" -> ok = row.count();
                case "failed" -> failed = row.count();
                case "timeout" -> timeout = row.count();
                case "cancelled" -> cancelled = row.count();
                case "interrupted" -> interrupted = row.count();
                case "running" -> running = row.count();
                default -> { }
            }
        }
        StatusCounts byStatus = new StatusCounts(ok, failed, timeout, cancelled, interrupted, running);
        long terminal = ok + failed + timeout + cancelled + interrupted;
        TelemetrySummaryMapper.Percentiles duration = summary.durationPercentiles(window.from(), window.to());
        TelemetrySummaryMapper.Percentiles ttft = summary.ttftPercentiles(window.from(), window.to());
        TelemetrySummaryMapper.Totals totals = summary.totals(window.from(), window.to());
        return new Overview(window.from(), window.to(), window.clamped(),
                terminal, byStatus,
                terminal == 0 ? 0 : (double) ok / terminal,
                duration == null ? null : duration.p50(),
                duration == null ? null : duration.p95(),
                ttft == null ? null : ttft.p50(),
                ttft == null ? null : ttft.p95(),
                totals == null ? 0 : totals.inputTokens(),
                totals == null ? 0 : totals.outputTokens(),
                totals == null ? 0 : totals.credits(),
                summary.countByKind(window.from(), window.to()),
                summary.countByModel(window.from(), window.to()),
                summary.countByErrorType(window.from(), window.to()),
                summary.countByTool(window.from(), window.to()));
    }

    public PageResult<RunSummary> page(Instant from, Instant to, String kind, String status,
                                Long userId, String model, PageQuery query) {
        Window window = window(from, to);
        List<RunSummary> items = runs.page(window.from(), window.to(), blankToNull(kind),
                        blankToNull(status), userId, blankToNull(model),
                        query.limit(), query.offset()).stream()
                .map(AiRunAdminService::toSummary)
                .toList();
        long total = runs.countPage(window.from(), window.to(), blankToNull(kind),
                blankToNull(status), userId, blankToNull(model));
        return PageResult.of(total, query, items);
    }

    /** 详情：轮次 + 步骤（按 seq）。**不含正文**，attrs 里也只有长度与计数。 */
    public RunDetail detail(String traceId) {
        AiRunMapper.AiRunRow row = runs.findByTraceId(traceId);
        if (row == null) {
            return null;
        }
        List<SpanView> views = new ArrayList<>();
        for (AiRunSpanMapper.AiRunSpanRow span : spans.findByTraceId(traceId)) {
            views.add(new SpanView(span.getSeq() == null ? 0 : span.getSeq(), span.getParentSeq(),
                    span.getKind(), span.getName(), span.getStatus(), span.getErrorType(),
                    span.getStartedAt(), span.getDurationMs(), span.getTtftMs(),
                    orZero(span.getInputTokens()), orZero(span.getOutputTokens()),
                    parseAttrs(span.getAttrsJson())));
        }
        List<PayloadView> payloadViews = payloads.findByTraceId(traceId).stream()
                .map(p -> new PayloadView(
                        p.getSeq() == null ? 0 : p.getSeq(),
                        p.getRequestBody(), p.getResponseBody(),
                        p.getRequestBytes() == null ? 0 : p.getRequestBytes(),
                        p.getResponseBytes() == null ? 0 : p.getResponseBytes(),
                        Boolean.TRUE.equals(p.getTruncated())))
                .toList();
        return new RunDetail(toSummary(row), parseAttrs(row.getAttrsJson()), views,
                payloadViews, properties.isPayloadEnabled());
    }

    /** 时间窗内出现过的 kind / status / model，供筛选下拉。 */
    public Map<String, Object> meta(Instant from, Instant to) {
        Window window = window(from, to);
        return Map.of(
                "kinds", summary.distinctKinds(window.from(), window.to()),
                "models", summary.distinctModels(window.from(), window.to()),
                "statuses", summary.countByStatus(window.from(), window.to()),
                "from", window.from(),
                "to", window.to());
    }

    private static RunSummary toSummary(AiRunMapper.AiRunRow row) {
        return new RunSummary(row.getTraceId(), row.getUserId() == null ? 0 : row.getUserId(),
                row.getConversationId(), row.getKind(), row.getHarness(), row.getModel(),
                row.getStatus(), row.getErrorType(), row.getStartedAt(), row.getEndedAt(),
                row.getDurationMs(), row.getTtftMs(), orZero(row.getLlmRounds()),
                orZero(row.getToolCalls()), orZero(row.getInputTokens()),
                orZero(row.getOutputTokens()), orZero(row.getCredits()));
    }

    /** attrs 坏了当空——一个字段解析失败不该让整条记录打不开。 */
    private static Map<String, Object> parseAttrs(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return JSON.readValue(json, new TypeReference<Map<String, Object>>() { });
        } catch (Exception failure) {
            return Map.of();
        }
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
