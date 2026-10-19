package io.github.xiaou61.ai.internal.telemetry;

import io.github.xiaou61.platform.BizException;
import java.util.concurrent.TimeoutException;

/**
 * 异常 → **低基数错误标识**（WORK-022）。
 *
 * <p>为什么不能直接写异常消息：它可能含上游 URL、响应片段、甚至回显的 Authorization 头。
 * 统计页要的是"哪类错误多"，不是一份散落的日志。所以这里只回枚举名。
 *
 * <p>无法识别的一律 {@code _other}：宁可粗，不可漏敏感内容。
 */
final class TelemetryErrorTypes {

    static final String OTHER = "_other";

    private TelemetryErrorTypes() {
    }

    static String of(Throwable failure) {
        if (failure == null) {
            return OTHER;
        }
        if (failure instanceof TimeoutException) {
            return "timeout";
        }
        if (failure instanceof BizException biz) {
            // ErrorCode 的 name 本身就是低基数标识（INVALID_ARGUMENT / INSUFFICIENT_CREDITS / ...）
            return biz.errorCode() == null ? OTHER : biz.errorCode().name();
        }
        return OTHER;
    }
}
