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
}
