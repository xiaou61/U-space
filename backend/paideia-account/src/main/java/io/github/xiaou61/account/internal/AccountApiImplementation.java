package io.github.xiaou61.account.internal;

import io.github.xiaou61.account.AccountApi;
import io.github.xiaou61.account.Identity;
import io.github.xiaou61.account.LearnerProfile;
import io.github.xiaou61.account.internal.profile.LearnerProfileService;
import io.github.xiaou61.account.internal.user.UserMapper;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * {@link AccountApi} 的实现。放在 internal 里，其它模块只能看到包根的接口。
 *
 * <p>参数是字符串形式的 id（与令牌的 `sub` 一致），所以要处理非法数字的情况——
 * 调用方传进来的可能是任何东西，不能让它变成 500。
 */
@Component
class AccountApiImplementation implements AccountApi {

    private final UserMapper userMapper;
    private final LearnerProfileService profiles;

    AccountApiImplementation(UserMapper userMapper, LearnerProfileService profiles) {
        this.userMapper = userMapper;
        this.profiles = profiles;
    }

    @Override
    public Optional<String> findRoleById(String userId) {
        // 注释承诺"调用方传进来的可能是任何东西"，而 Long.valueOf(null) 抛的是 NPE、
        // 不是 NumberFormatException——只 catch 后者的话 null 会冒泡成 500。
        Long id = parseId(userId);
        return id == null ? Optional.empty() : userMapper.findRoleById(id);
    }

    /**
     * 读画像。
     *
     * <p>先确认账号存在：**现存**但没有画像行的账号要拿到默认值，**不存在**的账号要拿到空。
     * 两者不能靠 `learner_profiles` 有没有行来区分（新用户本来就没有那一行）。
     */
    @Override
    public Optional<LearnerProfile> findProfile(String userId) {
        Long id = parseId(userId);
        if (id == null || userMapper.findById(id).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(profiles.get(id));
    }

    @Override
    public LearnerProfile saveProfile(String userId, Identity identity, String targetRole,
                                      String targetCity, String timeline, String stage) {
        Long id = parseId(userId);
        if (id == null || userMapper.findById(id).isEmpty()) {
            throw new BizException(ErrorCode.NOT_FOUND, "账号不存在");
        }
        return profiles.save(id, identity, targetRole, targetCity, timeline, stage);
    }

    /** 非法 id 一律当作"没有这个账号"，不把字符串解析失败变成 500。 */
    private static Long parseId(String userId) {
        if (userId == null) {
            return null;
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
