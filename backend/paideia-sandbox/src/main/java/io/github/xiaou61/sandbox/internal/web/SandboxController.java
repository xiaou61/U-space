package io.github.xiaou61.sandbox.internal.web;

import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import io.github.xiaou61.sandbox.internal.audit.SandboxQuotaService;
import io.github.xiaou61.sandbox.internal.run.CodeRun;
import io.github.xiaou61.sandbox.internal.run.CodeRunner;
import io.github.xiaou61.sandbox.internal.run.RunSlot;
import io.github.xiaou61.sandbox.internal.run.SandboxProperties;
import io.github.xiaou61.sandbox.internal.run.SqlDatasets;
import io.github.xiaou61.sandbox.internal.trace.SandboxTraceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 代码执行端点（WORK-015）。
 *
 * <p><b>权限口径</b>：挂在 {@code /api/v1/sandbox/**}，只需"已登录"——它既不是管理功能，
 * 也不能免登录（执行别人写的代码要能追溯）。归属一律取自令牌，请求体里没有 userId。
 *
 * <p><b>它不判题</b>：只回实际输出。期望值留在客户端，所以服务端没有题库、也没有"隐藏用例"，
 * 也就不会出现"服务端知道答案但用户可以猜到"这类问题。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/sandbox")
class SandboxController {

    private final SandboxProperties properties;
    private final CodeRunner runner;
    private final SandboxQuotaService quota;
    private final RunSlot slots;
    private final SandboxTraceService trace;
    private final SqlDatasets datasets;

    SandboxController(SandboxProperties properties, CodeRunner runner,
                      SandboxQuotaService quota, RunSlot slots,
                      SandboxTraceService trace, SqlDatasets datasets) {
        this.properties = properties;
        this.runner = runner;
        this.quota = quota;
        this.slots = slots;
        this.trace = trace;
        this.datasets = datasets;
    }

    /**
     * 能力与用量。
     *
     * <p>前端先问它，再决定要不要渲染「运行」：能力不可用时按钮根本不该存在，
     * 而不是点了才收到一个 503。
     */
    @GetMapping("/status")
    ApiResponse<StatusView> status() {
        long userId = currentUserId();
        boolean ready = runner.ready();
        // 配额按语言分档：SQL 20 次/天，其它 50 次/天（WORK-032 U-09）。
        // 两个数**分开给**——合成一个会让用户以为额度是共用的。
        return ApiResponse.ok(new StatusView(
                properties.isEnabled(),
                ready,
                properties.getRuntime().isBlank() ? null : properties.getRuntime(),
                List.copyOf(properties.languages()),
                properties.getDailyLimit(),
                quota.usedToday(userId),
                properties.getSqlDailyLimit(),
                quota.usedToday(userId, "sql"),
                // 只报**公开**数据集：隐藏集进了选择器，用户跑一句 SELECT * 就拿到内容，
                // 隐藏当场失效。运行端点仍认全部 key（题面本来就明文写着它）。
                datasets.publicKeys(),
                ready ? null : runner.unavailableReason()));
    }

    @PostMapping("/run")
    ApiResponse<RunView> run(@Valid @RequestBody RunRequest body) {
        long userId = currentUserId();
        String language = body.language().trim().toLowerCase(java.util.Locale.ROOT);

        if (!properties.isSupported(language)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT,
                    "只支持 " + String.join(" / ", properties.languages()));
        }
        if (body.code().length() > properties.getMaxCodeChars()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT,
                    "代码超过 " + properties.getMaxCodeChars() + " 字符");
        }
        // SQL 的"输入"是**数据集 key**，不是自由文本：内容由服务端按白名单取。
        // 隐藏数据集因此不随题面下发——客户端只拿到 key，拿不到数据与期望结果。
        String stdin;
        if ("sql".equals(language)) {
            String dataset = body.dataset();
            if (dataset == null || dataset.isBlank()) {
                throw new BizException(ErrorCode.INVALID_ARGUMENT, "SQL 运行必须指定数据集");
            }
            stdin = datasets.sql(dataset);
            if (stdin == null) {
                throw new BizException(ErrorCode.INVALID_ARGUMENT, "未知数据集：" + dataset);
            }
        } else {
            stdin = body.stdin() == null ? "" : body.stdin();
            if (stdin.length() > properties.getMaxStdinChars()) {
                throw new BizException(ErrorCode.INVALID_ARGUMENT,
                        "输入超过 " + properties.getMaxStdinChars() + " 字符");
            }
        }
        if (!runner.ready()) {
            quota.record(userId, body.entryId(), body.caseId(), language,
                    CodeRun.Status.DISABLED, null, 0, 0, false, body.code());
            throw new BizException(ErrorCode.SANDBOX_DISABLED,
                    runner.unavailableReason() == null ? "代码执行不可用" : runner.unavailableReason());
        }

        // 配额超了也要留审计：否则用户说"我明明没跑几次"时无从对账。
        // 审计写 rejected，而 countSince 排除 rejected —— 重试不会把自己永久锁死。
        try {
            quota.requireQuota(userId, language);
        } catch (BizException rejected) {
            quota.record(userId, body.entryId(), body.caseId(), language,
                    CodeRun.Status.REJECTED, null, 0, 0, false, body.code());
            throw rejected;
        }

        // explain 只对 SQL 有意义（WORK-035 T-05）：别的语言没有"执行计划"这个概念，
        // 静默忽略会让调用方以为拿到了计划而其实跑了一遍代码。
        boolean explain = body.explain() != null && body.explain();
        if (explain && !"sql".equals(language)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "只有 SQL 支持查看执行计划");
        }

        CodeRun.Result result;
        try (RunSlot.Slot ignored = slots.acquire()) {
            result = runner.run(new CodeRun.Request(language, body.code(), stdin, body.caseId(), explain));
        } catch (BizException rejected) {
            // 并发被拒：写一条 rejected 审计（不计入次数，见 countSince）
            quota.record(userId, body.entryId(), body.caseId(), language,
                    CodeRun.Status.REJECTED, null, 0, 0, false, body.code());
            throw rejected;
        }

        int outputBytes = result.stdout().getBytes(java.nio.charset.StandardCharsets.UTF_8).length
                + result.stderr().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        quota.record(userId, body.entryId(), body.caseId(), language,
                SandboxQuotaService.statusOf(result), result.exitCode(), result.durationMs(),
                outputBytes, result.truncated(), body.code());

        return ApiResponse.ok(new RunView(result.stdout(), result.stderr(), result.exitCode(),
                result.durationMs(), result.timedOut(), result.outOfMemory(), result.truncated(),
                result.tables().stream()
                        .map(table -> new TableView(table.statementIndex(), table.columns(), table.rows()))
                        .toList()));
    }

    /**
     * 逐步录制（WORK-023 REQ-001）。
     *
     * <p>与 `/run` **同一套鉴权、配额、并发槽与审计**：录制跑的也是用户代码，
     * 只是多记了一些东西。不另开口子——那会让"配额"变成一张可以绕过的纸。
     *
     * <p>只支持 Python：Java 要 JDI + 双 JVM + 调试端口，一期明确不做（REQ 非目标）。
     * 不支持时返回 **422 + 明确原因**，前端据此退回"一次性运行"（REQ-012）。
     */
    @PostMapping("/trace")
    ApiResponse<TraceView> trace(@Valid @RequestBody RunRequest body) {
        long userId = currentUserId();
        String language = body.language().trim().toLowerCase(java.util.Locale.ROOT);

        if (!properties.isSupported(language)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "只支持 java 与 python");
        }
        if (body.code().length() > properties.getMaxCodeChars()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT,
                    "代码超过 " + properties.getMaxCodeChars() + " 字符");
        }
        String stdin = body.stdin() == null ? "" : body.stdin();
        if (stdin.length() > properties.getMaxStdinChars()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT,
                    "输入超过 " + properties.getMaxStdinChars() + " 字符");
        }
        if (!runner.ready()) {
            throw new BizException(ErrorCode.SANDBOX_DISABLED,
                    runner.unavailableReason() == null ? "代码执行不可用" : runner.unavailableReason());
        }

        try {
            quota.requireQuota(userId);
        } catch (BizException rejected) {
            quota.record(userId, body.entryId(), body.caseId(), language,
                    CodeRun.Status.REJECTED, null, 0, 0, false, body.code());
            throw rejected;
        }

        SandboxTraceService.Trace result;
        try (RunSlot.Slot ignored = slots.acquire()) {
            result = trace.trace(language, body.code(), stdin);
        } catch (UnsupportedOperationException unsupported) {
            // 降级不是错误：前端会退回"一次性运行"，所以给可读的原因而不是 500
            throw new BizException(ErrorCode.INVALID_ARGUMENT, unsupported.getMessage());
        } catch (BizException rejected) {
            quota.record(userId, body.entryId(), body.caseId(), language,
                    CodeRun.Status.REJECTED, null, 0, 0, false, body.code());
            throw rejected;
        }

        // 超时与否看 reason：录制器在超时被杀时会把 "time_limit" 写进最后一条能读到的 end 事件
        boolean timedOut = "time_limit".equals(result.reason());
        quota.record(userId, body.entryId(), body.caseId(), language,
                timedOut ? CodeRun.Status.TIMEOUT : CodeRun.Status.OK,
                null, result.durationMs(), 0, result.truncated(), body.code());
        return ApiResponse.ok(new TraceView(result.steps(), result.output(),
                result.reason(), result.truncated(), result.durationMs()));
    }

    record TraceView(List<SandboxTraceService.TraceStep> steps, String output,
                     String reason, boolean truncated, long durationMs) {
    }

    record StatusView(boolean enabled, boolean ready, String runtime, List<String> languages,
                      int dailyLimit, int usedToday,
                      // SQL 单独一档（WORK-032 U-09）：额度不与其它语言共用
                      int sqlDailyLimit, int sqlUsedToday,
                      // 已知数据集 key：客户端据此渲染选择器，内容仍由服务端持有
                      List<String> datasets,
                      String reason) {
    }

    record RunRequest(
            @NotBlank @Size(max = 16) String language,
            @NotBlank String code,
            @Size(max = 4_000) String stdin,
            // SQL 专用：数据集 key，服务端按白名单解析成建表与插数
            @Size(max = 64) String dataset,
            // code_runs.entry_id 是 BIGINT UNSIGNED：负数会让审计 INSERT 报 Out of range，
            // 而那时代码已经真的跑完了 —— 除了 500，countSince 还会漏记，等于送了一条免配额通道。
            @Positive Long entryId,
            @Size(max = 64) String caseId,
            // SQL 专用（WORK-035 T-05）：true 时跑 EXPLAIN FORMAT=TREE 而不是执行语句。
            // 可空：老客户端不带这个字段时按 false 处理。
            Boolean explain) {
    }

    /**
     * 令牌主体 → 账号 id。合法令牌必然是数字（账号主键自增），非数字只可能来自伪造或版本错配。
     */
    private static long currentUserId() {
        try {
            return Long.parseLong(io.github.xiaou61.security.CurrentUser.subjectId());
        } catch (NumberFormatException cause) {
            throw new BizException(ErrorCode.UNAUTHENTICATED, "令牌主体不是有效的账号标识");
        }
    }

    /** 一个结果集（WORK-032）。只有 SQL 会返回；其它语言是空数组。 */
    record TableView(int statementIndex, List<String> columns, List<List<String>> rows) {
    }

    record RunView(String stdout, String stderr, int exitCode, long durationMs,
                   boolean timedOut, boolean outOfMemory, boolean truncated,
                   List<TableView> tables) {
    }
}
