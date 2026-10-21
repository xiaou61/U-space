package io.github.xiaou61.account.internal;

import io.github.xiaou61.account.AccountAdminApi;
import io.github.xiaou61.account.internal.token.RefreshTokenMapper;
import io.github.xiaou61.account.internal.user.AccountDeletionService;
import io.github.xiaou61.account.internal.user.UserMapper;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import io.github.xiaou61.platform.PageQuery;
import io.github.xiaou61.platform.PageResult;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账号模块的管理端用例（WORK-030）。
 *
 * <p><b>不可自伤在这里判</b>（REQ-010）：改角色与删号都要比对"目标 id == 操作者 id"。
 * 只在前端藏按钮挡不住直接调接口。
 *
 * <p>**这里不判断"调用者是不是管理员"**：那是路径规则的事（`hasRole("ADMIN")`）。
 * 这一层只负责业务约束，否则同一个判断会散落在两处，将来改一处漏一处。
 */
@Service
class AccountAdminApiImplementation implements AccountAdminApi {

    /** 可选角色。写成白名单而不是"非空即可"：`role` 决定鉴权，写进一个认不出的值 = 那个人登录后没有任何权限。 */
    private static final Set<String> ROLES = Set.of("admin", "learner");

    private final UserMapper users;
    private final RefreshTokenMapper refreshTokens;
    private final AccountDeletionService deletions;

    AccountAdminApiImplementation(UserMapper users,
                                  RefreshTokenMapper refreshTokens,
                                  AccountDeletionService deletions) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.deletions = deletions;
    }

    @Override
    public PageResult<AdminUser> list(String query, String role, int page, int size) {
        String needle = query == null || query.isBlank() ? null : query.trim();
        String roleFilter = role == null || role.isBlank() ? null : requireRole(role);
        PageQuery pageQuery = PageQuery.of(page, size, null);

        long total = users.countAdmin(needle, roleFilter);
        if (total == 0L) {
            return PageResult.of(0L, pageQuery, List.of());
        }
        List<AdminUser> items = users
                .pageAdmin(needle, roleFilter, pageQuery.limit(), pageQuery.offset())
                .stream()
                .map(row -> new AdminUser(row.getId(), row.getUsername(), row.getEmail(), row.getRole(),
                        row.getCreatedAt() == null ? null : row.getCreatedAt().toString()))
                .toList();
        return PageResult.of(total, pageQuery, items);
    }

    @Override
    public Optional<AdminUser> findById(long id) {
        return users.findAdminById(id)
                .map(row -> new AdminUser(row.getId(), row.getUsername(), row.getEmail(), row.getRole(),
                        row.getCreatedAt() == null ? null : row.getCreatedAt().toString()));
    }

    @Override
    @Transactional
    public void changeRole(long id, String role, long operatorId) {
        requireNotSelf(id, operatorId, "不能改自己的角色");
        String next = requireRole(role);
        if (users.updateRole(id, next) == 0) {
            throw new BizException(ErrorCode.NOT_FOUND, "账号不存在");
        }
        // 用户裁决 U-18：降级要"立刻没有权限"。访问令牌无状态改不了，但它分钟级过期；
        // refresh 一删，最长一轮刷新内必定生效。
        refreshTokens.deleteByUser(id);
    }

    @Override
    public void deleteByAdmin(long id, long operatorId) {
        requireNotSelf(id, operatorId, "不能删除自己的账号");
        deletions.deleteByAdmin(id);
    }

    private static void requireNotSelf(long id, long operatorId, String message) {
        if (id == operatorId) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, message);
        }
    }

    private static String requireRole(String role) {
        String normalized = role.trim().toLowerCase(java.util.Locale.ROOT);
        if (!ROLES.contains(normalized)) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "角色只能是 admin 或 learner");
        }
        return normalized;
    }
}
