package io.github.xiaou61.ai;

/**
 * 一轮调用的采集端口（WORK-022）。
 *
 * <p><b>这是本项唯一重要的设计决定</b>：业务代码只认识这个接口，不认识数据库、OTel 或任何观测后端。
 * 落地由若干 {@code RunSink} 并列完成——接 Langfuse / Prometheus 是加一个类，不是回头改七个埋点。
 *
 * <p><b>调用方线程零 IO</b>：`begin` / `step` / `attribute` / `finish` / `close` 只动内存，
 * 快照进有界队列就返回。队列满时丢弃并计数，**统计系统不许影响回答**。
 *
 * <p><b>失败只打日志</b>：写入抛异常一律吞掉。调用方永远不该因为"记录失败"而看到错误。
 *
 * <p>用法：一轮的开头 `begin`，结束时 `finish`；异常路径靠 try-with-resources 的
 * {@code close()} 兜成 {@link RunOutcome#INTERRUPTED}。
 *
 * <pre>{@code
 * try (var run = recorder.begin(RunSpec.of(RunKind.CHAT, userId))) {
 *     try (var step = run.step(SpanSpec.llm(1))) {
 *         ... 一次上游调用 ...
 *         step.tokens(input, output, 0);
 *         step.succeeded();
 *     }
 *     run.tokens(input, output, 0);
 *     run.finish(RunOutcome.OK, null);
 * }
 * }</pre>
 */
public interface RunRecorder {

    /** 采集是否开着。关掉时实现返回 {@link NoopScope}，业务代码不必分支。 */
    boolean enabled();

    RunScope begin(RunSpec spec);

    /** 一轮。实现必须幂等：`finish` 与 `close` 任意顺序、任意次数，只生效一次。 */
    interface RunScope extends AutoCloseable {

        /** 开一步。没在 try-with-resources 里 close 的步按 interrupted 处理。 */
        StepScope step(SpanSpec spec);

        /**
         * 冷字段。**只接受白名单键**（见设计里那七个），其它键记 debug 后丢弃——
         * 不然总有一天有人把提示词或用户输入塞进来。
         */
        void attribute(String key, Object value);

        /** 累加到轮次总量。 */
        void tokens(int input, int output, int cacheRead);

        /** 首字时刻。只在第一个正文增量出现时调，重复调用以第一次为准。 */
        void markTtft();

        /** 这一轮实扣多少积分（与积分流水做日级对账）。 */
        void addCredits(int charged);

        /** 终态 + 低基数错误标识（取自 ErrorCode 或归一值，**不是异常消息**）。 */
        void finish(RunOutcome outcome, String errorType);

        @Override
        void close();
    }

    /** 一步。 */
    interface StepScope extends AutoCloseable {
        void attribute(String key, Object value);

        void tokens(int input, int output, int cacheRead);

        void succeeded();

        void failed(String errorType);

        @Override
        void close();
    }
}
