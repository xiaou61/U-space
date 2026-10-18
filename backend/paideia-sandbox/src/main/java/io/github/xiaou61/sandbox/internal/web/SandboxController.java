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

    SandboxController(SandboxProperties properties, CodeRunner runner,
                      SandboxQuotaService quota, RunSlot slots) {
        this.properties = properties;
        this.runner = runner;
        this.quota = quota;
        this.slots = slots;
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
        return ApiResponse.ok(new StatusView(
                properties.isEnabled(),
                ready,
                properties.getRuntime().isBlank() ? null : properties.getRuntime(),
                List.copyOf(properties.languages()),
                properties.getDailyLimit(),
                quota.usedToday(userId),
                ready ? null : runner.unavailableReason()));
    }

    @PostMapping("/run")
    ApiResponse<RunView> run(@Valid @RequestBody RunRequest body) {
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
            quota.record(userId, body.entryId(), body.caseId(), language,
                    CodeRun.Status.DISABLED, null, 0, 0, false, body.code());
            throw new BizException(ErrorCode.SANDBOX_DISABLED,
                    runner.unavailableReason() == null ? "代码执行不可用" : runner.unavailableReason());
        }

        // 配额超了也要留审计：否则用户说"我明明没跑几次"时无从对账。
        // 审计写 rejected，而 countSince 排除 rejected —— 重试不会把自己永久锁死。
        try {
            quota.requireQuota(userId);
        } catch (BizException rejected) {
            quota.record(userId, body.entryId(), body.caseId(), language,
                    CodeRun.Status.REJECTED, null, 0, 0, false, body.code());
            throw rejected;
        }

        CodeRun.Result result;
        try (RunSlot.Slot ignored = slots.acquire()) {
            result = runner.run(new CodeRun.Request(language, body.code(), stdin, body.caseId()));
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
                result.durationMs(), result.timedOut(), result.outOfMemory(), result.truncated()));
    }

    record StatusView(boolean enabled, boolean ready, String runtime, List<String> languages,
                      int dailyLimit, int usedToday, String reason) {
    }

    record RunRequest(
            @NotBlank @Size(max = 16) String language,
            @NotBlank String code,
            @Size(max = 4_000) String stdin,
            // code_runs.entry_id 是 BIGINT UNSIGNED：负数会让审计 INSERT 报 Out of range，
            // 而那时代码已经真的跑完了 —— 除了 500，countSince 还会漏记，等于送了一条免配额通道。
            @Positive Long entryId,
            @Size(max = 64) String caseId) {
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

    record RunView(String stdout, String stderr, int exitCode, long durationMs,
                   boolean timedOut, boolean outOfMemory, boolean truncated) {
    }
}
