package io.github.xiaou61.account.internal.oauth;

import io.github.xiaou61.account.internal.token.AuthTokens;
import io.github.xiaou61.account.internal.token.TokenService;
import io.github.xiaou61.account.internal.user.User;
import io.github.xiaou61.account.internal.user.UserMapper;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import io.github.xiaou61.security.AuthPort;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 第三方登录的业务规则：找到账号或建一个、绑定、解绑。
 *
 * <p>三条不能省的规则：
 *
 * <ul>
 *   <li><b>身份只由 {@code (provider, providerUserId)} 决定</b>。邮箱相同**不自动合并**——
 *       合并意味着"谁先注册这个邮箱谁就拿下这个第三方账号"，而那正是账号劫持的经典路径。</li>
 *   <li><b>不能解掉最后一种登录方式</b>。解完就再也进不来了，而这个系统的注销是不可恢复的，
 *       两者叠加等于把账号变成一块删不掉的石头。</li>
 *   <li><b>绑定冲突不区分原因</b>。这个第三方身份已被别人占用，与平台出错，给用户看到的是
 *       同一句"授权失败"，否则接口就成了"某人的 GitHub 是否注册过"的查询器。</li>
 * </ul>
 */
@Service
public class OAuthLoginService {

    private static final Logger log = LoggerFactory.getLogger(OAuthLoginService.class);

    private final UserMapper users;
    private final UserIdentityMapper identities;
    private final UsernameAllocator usernames;
    private final TokenService tokens;

    OAuthLoginService(UserMapper users, UserIdentityMapper identities,
                      UsernameAllocator usernames, TokenService tokens) {
        this.users = users;
        this.identities = identities;
        this.usernames = usernames;
        this.tokens = tokens;
    }

    /**
     * 登录或首次建号。整个动作在一个事务里：建了号却没建成绑定，会留下一个谁都登不进的账号。
     */
    @Transactional
    public AuthTokens loginOrRegister(OAuthProvider provider, OAuthProfile profile) {
        Optional<UserIdentity> existing =
                identities.findByProviderAndSubject(provider.key(), profile.providerUserId());
        if (existing.isPresent()) {
            User user = users.findById(existing.get().getUserId())
                    .orElseThrow(() -> new BizException(ErrorCode.UNAUTHENTICATED, "授权失败，请重试"));
            // displayName 不在这里更新：用户可能已经在站内改过昵称，登录时回写会把它盖掉
            return tokens.issueFor(user);
        }

        User user = createAccount(profile);
        try {
            identities.insert(identityOf(user.getId(), provider, profile));
        } catch (DuplicateKeyException exception) {
            // 同一瞬间两次授权：唯一索引兜底。此时另一个请求已经建好了绑定，
            // 本请求不能再建第二个账号——按"授权失败"回滚，用户重试即可登进那个账号。
            throw new BizException(ErrorCode.CONFLICT, "授权失败，请重试");
        }
        log.info("第三方首次登录建号：provider={} userId={}", provider.key(), user.getId());
        return tokens.issueFor(user);
    }

    /**
     * 把第三方账号绑到已登录的用户身上。
     *
     * @throws BizException 该身份已被占用（与"授权失败"同一句文案）
     */
    @Transactional
    public void bind(long userId, OAuthProvider provider, OAuthProfile profile) {
        Optional<UserIdentity> existing =
                identities.findByProviderAndSubject(provider.key(), profile.providerUserId());
        if (existing.isPresent()) {
            if (existing.get().getUserId().equals(userId)) {
                return;   // 已经绑过了，幂等
            }
            log.warn("第三方绑定冲突：provider={} 已被 userId={} 占用，请求方 userId={}",
                    provider.key(), existing.get().getUserId(), userId);
            throw new BizException(ErrorCode.CONFLICT, "授权失败，请重试");
        }
        users.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "账号不存在"));
        try {
            identities.insert(identityOf(userId, provider, profile));
        } catch (DuplicateKeyException exception) {
            throw new BizException(ErrorCode.CONFLICT, "授权失败，请重试");
        }
    }

    /**
     * 解绑。
     *
     * @throws BizException 解完就没有任何一种登录方式了
     */
    @Transactional
    public void unbind(long userId, OAuthProvider provider) {
        User user = users.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "账号不存在"));
        int bound = identities.countByUserId(userId);
        boolean hasPassword = user.getPasswordHash() != null;

        // 剩下的方式 = 其它绑定 + 有没有密码。当前这个 provider 一定在 bound 里（否则下面 delete 是空操作）。
        int remaining = (bound - 1) + (hasPassword ? 1 : 0);
        if (remaining <= 0) {
            throw new BizException(ErrorCode.CONFLICT,
                    "这是你唯一的登录方式，解绑后将无法登录。请先设置密码或绑定其它账号。");
        }
        identities.deleteByUserIdAndProvider(userId, provider.key());
    }

    /** 账号设置页要看的：绑了哪些、有没有密码。 */
    public Bindings bindingsOf(long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "账号不存在"));
        List<UserIdentity> bound = identities.findByUserId(userId);
        return new Bindings(bound, user.getPasswordHash() != null);
    }

    public record Bindings(List<UserIdentity> identities, boolean hasPassword) {
    }

    private User createAccount(OAuthProfile profile) {
        User user = new User();
        user.setUsername(usernames.allocate(profile.nickname()));
        // 邮箱只在平台确实给出时才存：GitHub 用户可以隐藏邮箱，那时这里就是 null
        user.setEmail(blankToNull(profile.email()));
        // 第三方注册没有密码。**不是**随机密码：随机密码会让"忘记密码"这条路看起来可用，
        // 而系统里根本没有找回密码（WORK-003 裁决）。
        user.setPasswordHash(null);
        user.setRole(AuthPort.ROLE_LEARNER);
        // 平台不给"该邮箱是否已验证"的标志，所以这一列留空而不是填当前时间：
        // 填时间等于记一条"我们验证过"的假账。它目前不参与任何判定（见迁移注释）。
        user.setEmailVerifiedAt(null);
        try {
            users.insert(user);
        } catch (DuplicateKeyException exception) {
            // 用户名被并发抢走：重试一次带后缀的名字。这里不循环——重试仍失败说明是别的冲突
            user.setUsername(usernames.allocate(profile.nickname() + "-" + System.nanoTime() % 1000));
            users.insert(user);
        }
        return user;
    }

    private static UserIdentity identityOf(long userId, OAuthProvider provider, OAuthProfile profile) {
        UserIdentity identity = new UserIdentity();
        identity.setUserId(userId);
        identity.setProvider(provider.key());
        identity.setProviderUserId(profile.providerUserId());
        identity.setDisplayName(blankToNull(profile.nickname()));
        return identity;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
