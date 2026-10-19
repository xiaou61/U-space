package io.github.xiaou61.ai.internal.telemetry;

/**
 * 投递给写入线程的任务（WORK-022）。
 *
 * <p>只有两种：开始与结束。**步骤跟着"结束"一起走**——中间不投递，这样一轮最多两个任务，
 * 队列压力与轮数成正比而不是与步数成正比。
 */
sealed interface RunPayload {

    record RunBegin(RunRecord run) implements RunPayload {
    }

    record RunFinish(RunRecord run) implements RunPayload {
    }
}
