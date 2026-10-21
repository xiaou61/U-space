package io.github.xiaou61.account;

import io.github.xiaou61.platform.PageResult;
import java.util.Optional;

/**
 * 账号模块的**管理端**契约（WORK-030）。
 *
 * <p><b>为什么单开一个接口，而不是往 {@link AccountApi} 里加方法</b>：那个接口的每个方法都是
 * "任何已登录的人按自己的 id 读自己的东西"；这里每一个方法都是"管理员按别人的 id 读别人的东西"。
 * 两类能力混在一个接口里，等于让每个注入 {@code AccountApi} 的业务模块顺手拿到管理员能力。
 *
 * <p><b>不带 {@code passwordHash}</b>：{@link AdminUser} 是专门给管理端用的视图，
 * 与内部分页查询的列清单一一对应。内部分页查询**只选需要的列**，不复用会带出 hash 的既有 SELECT。
 */
public interface AccountAdminApi {

    /**
     * 分页查账号。
     *
     * @param query 用户名或邮箱的模糊片段；空串表示不过滤
     * @param role  `admin` / `learner`；为空表示不过滤
     * @param page  从 1 开始
     * @param size  每页条数，实现方必须夹到上限内
     */
    PageResult<AdminUser> list(String query, String role, int page, int size);

    /** 按 id 读一个账号。不存在时返回空。 */
    Optional<AdminUser> findById(long id);

    /**
     * 改角色并**吊销该用户全部 refresh token**（用户裁决 U-18）。
     *
     * <p>访问令牌是无状态的，改不了；但它分钟级过期，refresh 一删，最长一轮刷新内必定生效。
     *
     * @throws io.github.xiaou61.platform.BizException 角色不可选、账号不存在、或调的是自己
     */
    void changeRole(long id, String role, long operatorId);

    /**
     * 管理员删号：**不校验对方的密码**，其余与自助注销完全一致（共用同一段级联）。
     *
     * @throws io.github.xiaou61.platform.BizException 账号不存在、或删的是自己
     */
    void deleteByAdmin(long id, long operatorId);

    /** 管理端看到的账号。**没有 passwordHash 字段**——这一条由类型保证，不靠约定。 */
    record AdminUser(long id, String username, String email, String role, String createdAt) {
    }
}
