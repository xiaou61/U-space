package io.github.xiaou61.account.internal.web;

import io.github.xiaou61.account.internal.user.AccountDeletionService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import io.github.xiaou61.security.CurrentUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 注销端点。
 *
 * <p>路径放在 `/api/v1/me/**` 而不是 `/api/v1/auth/**`：后者在安全配置里是 **permitAll**，
 * 注销绝不能免鉴权。`/api/v1/me/**` 落到 `anyRequest().authenticated()`，正是想要的语义。
 *
 * <p>用 POST 而不是 DELETE：请求体里要带密码确认，而 DELETE 带体在不同客户端上表现不一。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/me")
class AccountDeletionController {

    record DeletionRequest(String password) {
    }

    private final AccountDeletionService deletion;

    AccountDeletionController(AccountDeletionService deletion) {
        this.deletion = deletion;
    }

    /**
     * 注销当前账号。**不可恢复**：会话、记忆、笔记与已发出的分享一并删除。
     *
     * @return 恒为 true（成功了就是成功了；失败会抛出去）
     */
    @PostMapping("/deletion")
    ApiResponse<Boolean> delete(@RequestBody(required = false) DeletionRequest request) {
        long userId = requireUserId();
        deletion.delete(userId, request == null ? null : request.password());
        return ApiResponse.ok(true);
    }

    private static long requireUserId() {
        try {
            return Long.parseLong(CurrentUser.subjectId());
        } catch (NumberFormatException | BizException exception) {
            throw new BizException(ErrorCode.UNAUTHENTICATED, "令牌主体不是有效的账号标识");
        }
    }
}
