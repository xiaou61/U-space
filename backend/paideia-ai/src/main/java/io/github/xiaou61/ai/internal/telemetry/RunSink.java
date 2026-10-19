package io.github.xiaou61.ai.internal.telemetry;

/**
 * 落地接口（WORK-022）。
 *
 * <p><b>这是"可扩展"的落点</b>：本项只实现 {@link MysqlRunSink}；接 Langfuse、Prometheus
 * 或任何观测后端都是**加一个实现类**，不改任何埋点、不改表结构。
 *
 * <p>实现必须自己吞掉异常——调用方（写队列线程）只负责继续处理下一条。
 */
interface RunSink {

    /**
     * 接收一个任务。
     *
     * <p>抛异常是允许的（写队列会记一条 WARN 并继续），但**不许重试**：
     * 库故障时重试只会把队列撑爆，让"统计挂了"升级成"应用挂了"。
     */
    void accept(RunPayload payload);
}
