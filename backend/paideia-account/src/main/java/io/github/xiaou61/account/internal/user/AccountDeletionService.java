package io.github.xiaou61.account.internal.user;

import io.github.xiaou61.account.internal.token.RefreshTokenMapper;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import io.github.xiaou61.platform.UserDeletedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 注销账号（用户裁决 10：**级联硬删、不保留、不可恢复**）。
 *
 * <p>三步，在同一个事务里：
 * <ol>
 *   <li>删掉这个人的 refresh 记录——留着等于留着一把能换新令牌的钥匙；</li>
 *   <li>删掉 users 那一行；</li>
 *   <li>发布 {@link UserDeletedEvent}，由各模块**自己**删掉自己那份用户数据。</li>
 * </ol>
 *
 * <p><b>要求密码</b>：注销不可逆，只凭一个被偷走的令牌就能清空一个人的全部数据，代价不对等。
 * 这里不校验"两次确认"之类的东西——那是界面的事，接口层守住"必须有凭据"这一条。
 *
 * <p>没有宽限期、没有回收站、也没有定时任务：删就是删（用户明确选了这一条）。
 */
@Service
public class AccountDeletionService {

    private final UserMapper users;
    private final RefreshTokenMapper refreshTokens;
    private final PasswordHasher hasher;
    private final ApplicationEventPublisher events;
    /** 画像与本模块同库同域，这里直接删（不绕事件）。 */
    private final io.github.xiaou61.account.internal.profile.LearnerProfileService profiles;

    AccountDeletionService(UserMapper users,
                           RefreshTokenMapper refreshTokens,
                           PasswordHasher hasher,
                           ApplicationEventPublisher events,
                           io.github.xiaou61.account.internal.profile.LearnerProfileService profiles) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.hasher = hasher;
        this.events = events;
        this.profiles = profiles;
    }

    @Transactional
    public void delete(long userId, String rawPassword) {
        User user = users.findById(userId).orElseThrow(() -> notFound());
        if (rawPassword == null || !hasher.matches(rawPassword, user.getPasswordHash())) {
            // 与"账号不存在"用同一条消息不合适（这里身份是已知的），但也不回显任何账号信息
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "密码不正确");
        }
        refreshTokens.deleteByUser(userId);
        // 画像与本模块同库同域（WORK-010）：直接删，不必绕一圈事件。
        // 其它模块的用户数据仍由 UserDeletedEvent 各自删除（方向是单向的）。
        profiles.deleteByUser(userId);
        users.deleteById(userId);
        // 事务提交前由各模块监听并删掉自己的数据（见 UserDeletedEvent 的说明）
        events.publishEvent(new UserDeletedEvent(userId));
    }

    private static BizException notFound() {
        return new BizException(ErrorCode.NOT_FOUND, "账号不存在");
    }
}
