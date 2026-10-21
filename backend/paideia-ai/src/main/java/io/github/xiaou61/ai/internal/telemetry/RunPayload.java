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

    /**
     * @param run      轮次终态快照
     * @param payloads 这一轮各 LLM 步的完整载荷。**跟着 finish 一起走**：
     *                 它只在结束后才有意义（步序号与用量都要对得上），
     *                 而单独投递会让"步骤已落库但载荷还在队列里"多一个中间状态。
     */
    record RunFinish(RunRecord run, java.util.List<PayloadRecord> payloads) implements RunPayload {
    }
}
