package io.github.xiaou61.sandbox.internal.trace;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import io.github.xiaou61.sandbox.internal.run.CodeRun;
import io.github.xiaou61.sandbox.internal.run.CodeRunner;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 逐步录制的编排（WORK-023 T1/T2）。
 *
 * <p>三件事：把录制器脚本送进沙箱、把 stderr 上的 JSON 行解析成事件、把上限与失败说清楚。
 *
 * <p><b>解析必须容忍不完整</b>：超时被杀时最后一行常常只有半截 JSON。
 * 那一行直接丢掉（而不是整批失败）——用户要的是"被杀之前发生了什么"，
 * 我们要的是"能读多少给多少"。
 */
@Service
public class SandboxTraceService {

    private static final ObjectMapper JSON = new ObjectMapper();

    /** 单次录制最多返回多少步。与录制器里的常量一致：那边是硬上限，这边是兜底。 */
    static final int MAX_STEPS = 300;

    /** 单次录制响应字节上限（REQ-004）。 */
    static final int MAX_BYTES = 256 * 1024;

    private final CodeRunner runner;
    private final SandboxTraceScript script;

    SandboxTraceService(CodeRunner runner, SandboxTraceScript script) {
        this.runner = runner;
        this.script = script;
    }

    /** 一个录制步。字段名与前端 schema 一致（REQ-002 / 质量要求「可维护」）。 */
    public record TraceStep(int line, Map<String, String> locals, String event) {
    }

    /** 录制结果。 */
    public record Trace(
            List<TraceStep> steps,
            String output,
            String reason,
            boolean truncated,
            long durationMs) {
    }

    /**
     * 录制一段 Python 代码。
     *
     * @param language 目前只支持 python；其它语言由调用方在进来之前就拒掉（REQ-012 降级）
     */
    public Trace trace(String language, String code, String stdin) {
        String normalized = language == null ? "" : language.trim().toLowerCase(java.util.Locale.ROOT);
        if (!"python".equals(normalized)) {
            throw new UnsupportedOperationException("暂时只支持 Python 的逐步录制");
        }

        CodeRun.TraceResult result = runner.trace(code, stdin, script.python());
        List<TraceStep> steps = new ArrayList<>();
        String reason = result.timedOut() ? "time_limit" : "done";
        boolean truncated = false;

        int bytes = 0;
        for (String line : result.events().split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            JsonNode node;
            try {
                node = JSON.readTree(trimmed);
            } catch (Exception malformed) {
                // 超时被杀时最后一行是半截 JSON：丢掉它，保留已录的部分
                continue;
            }
            String type = node.path("t").asString("");
            if ("line".equals(type)) {
                if (steps.size() >= MAX_STEPS) {
                    truncated = true;
                    break;
                }
                Map<String, String> locals = new LinkedHashMap<>();
                // Jackson 3 里对象属性的迭代器叫 properties()（2.x 的 fields() 已改名）
                node.path("locals").properties().forEach(entry ->
                        locals.put(entry.getKey(), entry.getValue().asString("")));
                steps.add(new TraceStep(node.path("line").asInt(0), locals, "line"));
            } else if ("exception".equals(type)) {
                // 异常也是一步：用户要看到"在第几行挂的、当时变量是什么"
                steps.add(new TraceStep(node.path("line").asInt(0),
                        Map.of("__error__", node.path("type").asString("Error")), "exception"));
            } else if ("end".equals(type)) {
                reason = node.path("reason").asString("done");
                truncated = truncated || node.path("truncated").asBoolean(false);
            }
            bytes += trimmed.length();
            if (bytes > MAX_BYTES) {
                truncated = true;
                break;
            }
        }

        return new Trace(steps, result.stdout(), reason, truncated, result.durationMs());
    }

    /** 给状态端点用：录制能力是否可用（沙箱关着时前端不该显示按钮）。 */
    public boolean available() {
        return runner.ready();
    }
}
