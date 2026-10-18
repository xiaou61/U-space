package io.github.xiaou61.account;

import java.util.Optional;

/**
 * 账号模块的对外契约。
 *
 * <p>其它业务模块（将来的 learning、content 等）要按 `userId` 关联数据时，
 * 依赖的是这里的方法，而不是账号表或 `internal` 里的实现。本项只暴露确实被需要的一件事：
 * 按 id 读角色。等第一个真实消费者出现时再加方法，不提前铺开。
 *
 * <p>WORK-010 加了第二个消费者（身份化）：推荐配置与面试面要读"这个人现在是谁"。
 * 所以这里多了 {@link #findProfile} / {@link #saveProfile}——**只暴露读写画像这件事**，
 * 不把 `learner_profiles` 表的结构或 mapper 放出去。
 */
public interface AccountApi {

    /**
     * 读某个账号的角色。
     *
     * @param userId 账号 id（字符串形式，与令牌里的 `sub` 一致）
     * @return 角色名（`admin` / `learner`）；账号不存在时返回空
     */
    Optional<String> findRoleById(String userId);

    /**
     * 读某个账号的身份与画像（WORK-010）。
     *
     * <p><b>有账号但没有画像行时返回默认值，而不是空</b>：默认身份 `job_seeker`、画像字段全空。
     * 这样每个新用户从注册那一刻起就有确定的身份，不必在注册链路里插一行——漏插的后果会是
     * "这个人静默地没有身份"，而界面上看不出来。
     *
     * <p>**账号不存在时返回空**（与 {@link #findRoleById} 一致），让"没这个账号"与
     * "有这个账号但没画像"可区分。
     *
     * @param userId 账号 id（字符串形式）
     */
    Optional<LearnerProfile> findProfile(String userId);

    /**
     * 写入某个账号的身份与画像（整行覆盖）。
     *
     * <p>`identity` **只接受可选的枚举值**（一期只有 `job_seeker`）：留位的身份不做界面，
     * 也不许通过接口写进来，否则会出现"库里是考研、界面上根本没有这个身份"的幽灵状态。
     *
     * @throws io.github.xiaou61.platform.BizException 身份不可选、或账号不存在
     */
    LearnerProfile saveProfile(String userId, Identity identity, String targetRole,
                               String targetCity, String timeline, String stage);
}
