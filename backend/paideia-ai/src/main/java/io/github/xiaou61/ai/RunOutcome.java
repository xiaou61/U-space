package io.github.xiaou61.ai;

/**
 * 一轮调用的终态（WORK-022）。
 *
 * <p>{@link #INTERRUPTED} 是**兜底**：调用方没显式 finish 就 close 了（异常路径、线程被打断），
 * 由实现补上。它存在的意义是"崩溃/卡死的那一轮不会一行都不留"。
 */
public enum RunOutcome {

    OK("ok"),
    FAILED("failed"),
    TIMEOUT("timeout"),
    CANCELLED("cancelled"),
    INTERRUPTED("interrupted");

    private final String key;

    RunOutcome(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
