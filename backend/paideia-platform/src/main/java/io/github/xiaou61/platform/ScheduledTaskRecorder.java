package io.github.xiaou61.platform;

import java.time.Duration;

/**
 * 定时任务的执行结果记录口（WORK-028 REQ-016）。
 *
 * <p><b>为什么放在 platform</b>：定时任务分散在 account / ai 两个模块，而运维指标只在
 * paideia-app 里落地。接口放公共契约、实现留在 app，两个模块都只依赖自己已有的
 * {@code paideia-platform}，不新增依赖方向。
 *
 * <p><b>实现必须允许缺失</b>：调用方一律用 {@code ObjectProvider} 注入——测试切片、
 * 局部启动场景里没有这个 bean 时，任务照常跑，只是不留痕。
 */
public interface ScheduledTaskRecorder {

    /**
     * 记录一次执行。
     *
     * @param name     任务名，用稳定的短名（如 {@code expired-state-purger}），
     *                 不要用类名——类改名不该让历史曲线断开
     * @param duration 耗时
     * @param error    失败时的异常；成功传 {@code null}
     */
    void record(String name, Duration duration, Throwable error);
}
