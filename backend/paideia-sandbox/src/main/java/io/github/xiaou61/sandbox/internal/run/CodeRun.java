package io.github.xiaou61.sandbox.internal.run;

/**
 * 执行一次运行请求 / 结果（WORK-015）。
 *
 * <p>这几个 record 是**模块的对外契约**：控制器、Docker 实现、测试里的假实现都只认它们。
 * 加字段时想清楚"服务端到底该不该知道这件事"——比如判定需要的"期望输出"**刻意不在这里**，
 * 它在客户端，服务端只回实际输出。
 */
public final class CodeRun {

    private CodeRun() {
    }

    /** 一次运行请求。`caseId` 只进审计，不参与执行。 */
    public record Request(String language, String code, String stdin, String caseId) {
    }

    /**
     * 一次运行结果。
     *
     * <p>`timedOut` / `oom` 是**结果**不是错误：程序崩了也是有价值的反馈，接口仍然 200。
     * 只有"根本没能执行"（配额、并发、沙箱不可用）才是错误。
     */
    /**
     * 录制结果（WORK-023）。
     *
     * <p>`events` 是录制器写在 stderr 上的原始行（每行一个 JSON）。**不在这里解析**：
     * 解析要能容忍"超时被杀、最后一行不完整"，那是服务层的事，执行器只负责搬运。
     */
    public record TraceResult(
            String stdout,
            String events,
            long durationMs,
            boolean timedOut) {
    }

    public record Result(
            String stdout,
            String stderr,
            int exitCode,
            long durationMs,
            boolean timedOut,
            boolean outOfMemory,
            boolean truncated) {
    }

    /** 审计用的状态分类。值是存进库的，**改名等于改数据**。 */
    public enum Status {
        OK,
        COMPILE_ERROR,
        RUNTIME_ERROR,
        TIMEOUT,
        OUT_OF_MEMORY,
        REJECTED,
        DISABLED
    }
}
