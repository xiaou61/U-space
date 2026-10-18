package io.github.xiaou61.platform;

/**
 * 业务异常。携带稳定错误码，由 web 层统一映射为 HTTP 状态与响应体。
 */
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;

    public BizException(ErrorCode errorCode) {
        super(errorCode.defaultMessage());
        this.errorCode = errorCode;
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /** 带原因的构造（WORK-015）：底层异常留着，日志里能看到，响应体里不会外泄。 */
    public BizException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}
