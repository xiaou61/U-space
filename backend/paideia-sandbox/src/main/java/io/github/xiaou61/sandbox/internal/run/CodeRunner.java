package io.github.xiaou61.sandbox.internal.run;

/**
 * 执行端口。只有一个实现类在生产里跑（Docker），但保留接口是因为：
 * ①控制器与配额测试需要一个不依赖 Docker 的假实现；②将来换 gVisor/微虚拟机时只换实现。
 */
public interface CodeRunner {

    /** 执行一次。**不抛业务异常**：超时、OOM、编译失败都是结果的一部分。 */
    CodeRun.Result run(CodeRun.Request request);

    /** 当前是否真的可用（Docker 在、镜像在、开关开着）。 */
    boolean ready();

    /** 不可用的原因，给状态端点用；可用时返回 null。 */
    String unavailableReason();

    /**
     * 录制一次逐步执行（WORK-023）。
     *
     * <p>默认实现抛"不支持"：录制是**可选能力**，换执行后端（比如将来的独立 runner 服务）
     * 时不该因为它没有录制就编译不过。调用方捕获后降级为"一次性运行"。
     */
    default CodeRun.TraceResult trace(String code, String stdin, String traceScript) {
        throw new UnsupportedOperationException("当前执行后端不支持逐步录制");
    }
}
