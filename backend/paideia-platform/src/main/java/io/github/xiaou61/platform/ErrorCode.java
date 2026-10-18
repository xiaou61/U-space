package io.github.xiaou61.platform;

/**
 * 稳定错误码。已发布的数值不再变更，新增只能追加。
 */
public enum ErrorCode {

    OK(0, "ok"),
    INVALID_ARGUMENT(40000, "请求参数不合法"),
    UNAUTHENTICATED(40100, "未认证"),
    FORBIDDEN(40300, "无权访问"),
    NOT_FOUND(40400, "资源不存在"),
    CONFLICT(40900, "状态冲突"),
    /** 触发限流。与 CONFLICT 分开是因为语义不同：前者是状态冲突，后者是"等一会儿再来"。 */
    TOO_MANY_REQUESTS(42900, "请求过于频繁"),
    /**
     * 积分不足（WORK-009）。
     *
     * <p>与 INVALID_ARGUMENT / CONFLICT 分开是**需求**：界面要据此给出"去充值 / 联系管理员"这类
     * 可操作提示，而不是一句泛泛的"参数不合法"。映射到 HTTP 402。
     */
    INSUFFICIENT_CREDITS(40200, "积分不足"),
    /**
     * 个性化记忆已关闭（WORK-013）。
     *
     * <p>与 INVALID_ARGUMENT 分开是**需求**：界面要能说清"是你自己关掉的记忆"，而不是一句泛泛的
     * "参数不合法"。映射到 HTTP 409——请求本身没问题，是当前设置不允许这件事。
     */
    MEMORY_DISABLED(40910, "个性化记忆已关闭"),
    /**
     * 代码执行不可用（WORK-015）。
     *
     * <p>与 INTERNAL 分开是**需求**：界面要能说清"是部署没开这个能力 / Docker 不在"，
     * 而不是一句"服务器内部错误"。映射到 HTTP 503——服务存在，但这个能力当下不可用。
     */
    SANDBOX_DISABLED(50300, "代码执行不可用"),
    INTERNAL(50000, "服务器内部错误");

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
