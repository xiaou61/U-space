package io.github.xiaou61.sandbox.internal.run;

import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;

/**
 * 沙箱不可用（Docker 不在、镜像缺失）。映射成 503——这不是用户的错，重试可能有用。
 */
class SandboxUnavailableException extends BizException {

    SandboxUnavailableException(String message) {
        super(ErrorCode.SANDBOX_DISABLED, message);
    }

    SandboxUnavailableException(String message, Throwable cause) {
        super(ErrorCode.SANDBOX_DISABLED, message, cause);
    }
}
