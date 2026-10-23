package io.github.xiaou61;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.github.xiaou61.account.internal.oauth.OAuthHttp;
import io.github.xiaou61.account.internal.oauth.OAuthLoginService;
import io.github.xiaou61.account.internal.oauth.OAuthProfile;
import io.github.xiaou61.account.internal.oauth.OAuthProvider;
import io.github.xiaou61.account.internal.oauth.UserIdentityMapper;
import io.github.xiaou61.account.internal.token.AuthTokens;
import io.github.xiaou61.account.internal.user.UserMapper;
import io.github.xiaou61.platform.BizException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import tools.jackson.databind.ObjectMapper;

/**
 * 第三方登录与绑定（WORK-037 AC-001..AC-006）。
 *
 * <p><b>平台被桩掉，其余全是真的</b>：{@link OAuthHttp} 换成替身返回构造好的平台响应，
 * 而用户表、绑定表、令牌签发、注销级联都走真实的 MySQL 与真实服务。
 * 这样验的是"我们这一侧的行为"，不依赖平台可用性、不消耗任何人的真实授权。
 *
 * <p>真实平台的连通性只能手验（见验证报告），那是另一件事。
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "PAIDEIA_TEST_DB_PASSWORD", matches = ".+")
class OAuthLoginIntegrationTest {

    private static final AtomicLong SEQUENCE = new AtomicLong(System.nanoTime() % 100_000);

    @Autowired
    OAuthLoginService oauth;

    @Autowired
    UserMapper users;

    @Autowired
    UserIdentityMapper identities;

    @Autowired
    JdbcTemplate jdbc;

    @MockitoBean
    OAuthHttp oauthHttp;

    private static final ObjectMapper JSON = new ObjectMapper();

    private String uniqueSubject() {
        return "sub-" + SEQUENCE.incrementAndGet();
    }

    private static OAuthProfile profile(String subject, String nickname, String email) {
        return new OAuthProfile(subject, nickname, email);
    }

    // ---------------------------------------------------------------- AC-001 / AC-002

    @Test
    @DisplayName("AC-001 首次 GitHub 授权建号，并留下一条 github 绑定")
    void createsAccountOnFirstGithubLogin() {
        String subject = uniqueSubject();

        AuthTokens issued = oauth.loginOrRegister(OAuthProvider.GITHUB,
                profile(subject, "octo-" + subject, subject + "@example.test"));

        assertThat(issued.accessToken()).isNotBlank();
        assertThat(issued.user().role()).isEqualTo("learner");
        long userId = Long.parseLong(issued.user().id());

        assertThat(identities.findByProviderAndSubject("github", subject))
                .get()
                .satisfies(binding -> assertThat(binding.getUserId()).isEqualTo(userId));

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT username, email, password_hash FROM users WHERE id = ?", userId);
        assertThat(row.get("username")).as("用户名必须生成出来，不能为空").isNotNull();
        assertThat(row.get("password_hash")).as("第三方注册不该有密码").isNull();
    }

    @Test
    @DisplayName("AC-002 同一流程对 Gitee 成立，且两者互不干扰")
    void createsAccountOnFirstGiteeLogin() {
        String subject = uniqueSubject();

        AuthTokens issued = oauth.loginOrRegister(OAuthProvider.GITEE,
                profile(subject, "gitee-" + subject, null));

        long userId = Long.parseLong(issued.user().id());
        assertThat(identities.findByProviderAndSubject("gitee", subject)).isPresent();
        assertThat(identities.findByProviderAndSubject("github", subject))
                .as("同一个 subject 在不同平台必须互不相干")
                .isEmpty();
        assertThat(identities.countByUserId(userId)).isEqualTo(1);
    }

    // ---------------------------------------------------------------- AC-003

    @Test
    @DisplayName("AC-003 同一平台同一账号再次授权不新建账号")
    void repeatAuthorizationReusesAccount() {
        String subject = uniqueSubject();

        String first = oauth.loginOrRegister(OAuthProvider.GITHUB, profile(subject, "again", null))
                .user().id();
        String second = oauth.loginOrRegister(OAuthProvider.GITHUB, profile(subject, "again", null))
                .user().id();

        assertThat(second).as("重复授权建了第二个账号，等于每次登录都是一次新注册").isEqualTo(first);
    }

    @Test
    @DisplayName("AC-003 昵称冲突时第二个账号拿到带后缀的名字")
    void usernameCollisionGetsSuffix() {
        String nickname = "collide-" + uniqueSubject();

        String firstUser = oauth.loginOrRegister(OAuthProvider.GITHUB,
                profile(uniqueSubject(), nickname, null)).user().id();
        String secondUser = oauth.loginOrRegister(OAuthProvider.GITHUB,
                profile(uniqueSubject(), nickname, null)).user().id();

        assertThat(secondUser).isNotEqualTo(firstUser);
        Map<String, Object> row = jdbc.queryForMap(
                "SELECT username FROM users WHERE id = ?", Long.parseLong(secondUser));
        assertThat((String) row.get("username")).startsWith(nickname).endsWith("-2");
    }

    // ---------------------------------------------------------------- AC-004 / AC-005

    @Test
    @DisplayName("AC-004 绑定后能列出来，解绑后消失")
    void bindThenUnbind() {
        long userId = Long.parseLong(oauth.loginOrRegister(OAuthProvider.GITHUB,
                profile(uniqueSubject(), "binder-" + uniqueSubject(), null)).user().id());

        String extraSubject = uniqueSubject();
        oauth.bind(userId, OAuthProvider.GITEE, profile(extraSubject, "extra", null));

        OAuthLoginService.Bindings bound = oauth.bindingsOf(userId);
        assertThat(bound.identities()).hasSize(2);
        assertThat(bound.hasPassword()).isFalse();

        oauth.unbind(userId, OAuthProvider.GITEE);
        assertThat(oauth.bindingsOf(userId).identities()).hasSize(1);
        assertThat(identities.findByProviderAndSubject("gitee", extraSubject)).isEmpty();
    }

    @Test
    @DisplayName("AC-005 唯一的登录方式不能解绑，否则这个人再也进不来")
    void cannotUnbindLastCredential() {
        long userId = Long.parseLong(oauth.loginOrRegister(OAuthProvider.GITHUB,
                profile(uniqueSubject(), "only-" + uniqueSubject(), null)).user().id());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> oauth.unbind(userId, OAuthProvider.GITHUB))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("唯一的登录方式");

        assertThat(oauth.bindingsOf(userId).identities())
                .as("拒绝之后绑定关系必须原样保留")
                .hasSize(1);
    }

    @Test
    @DisplayName("AC-005 有密码时最后一个第三方绑定可以解——密码仍是可用方式")
    void canUnbindLastOAuthWhenPasswordExists() {
        String subject = uniqueSubject();
        long userId = Long.parseLong(oauth.loginOrRegister(OAuthProvider.GITHUB,
                profile(subject, "haspw-" + uniqueSubject(), null)).user().id());
        // 模拟"用户后来设了密码"：本项没有设密码入口，所以直接写库
        jdbc.update("UPDATE users SET password_hash = ? WHERE id = ?", "bcrypt-placeholder", userId);

        oauth.unbind(userId, OAuthProvider.GITHUB);

        assertThat(oauth.bindingsOf(userId).identities()).isEmpty();
        assertThat(oauth.bindingsOf(userId).hasPassword()).isTrue();
    }

    // ---------------------------------------------------------------- 冲突与绑定占用

    @Test
    @DisplayName("已绑定他人的第三方身份不能被另一个人绑定，且文案不区分原因")
    void bindingTakenIdentityFails() {
        String subject = uniqueSubject();
        long owner = Long.parseLong(oauth.loginOrRegister(OAuthProvider.GITHUB,
                profile(subject, "owner-" + uniqueSubject(), null)).user().id());
        long intruder = Long.parseLong(oauth.loginOrRegister(OAuthProvider.GITHUB,
                profile(uniqueSubject(), "intruder-" + uniqueSubject(), null)).user().id());

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> oauth.bind(intruder, OAuthProvider.GITHUB, profile(subject, "x", null)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("授权失败");

        assertThat(identities.findByProviderAndSubject("github", subject))
                .get()
                .satisfies(binding -> assertThat(binding.getUserId()).as("绑定不能被抢走").isEqualTo(owner));
    }

    @Test
    @DisplayName("重复绑定同一个身份是幂等的，不报错也不产生第二行")
    void rebindingSameIdentityIsIdempotent() {
        String subject = uniqueSubject();
        long userId = Long.parseLong(oauth.loginOrRegister(OAuthProvider.GITHUB,
                profile(subject, "idem-" + uniqueSubject(), null)).user().id());

        oauth.bind(userId, OAuthProvider.GITHUB, profile(subject, "idem", null));

        assertThat(identities.countByUserId(userId)).isEqualTo(1);
    }

    @Test
    @DisplayName("平台没给稳定 id 时拒绝建号，不用昵称或邮箱凑一个")
    void unknownProfileCannotCreateAccount() {
        when(oauthHttp.fetchUser(any(), anyString()))
                .thenReturn(JSON.readTree("{\"login\":\"someone\",\"email\":\"a@b.test\"}"));

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> OAuthProfile.from(JSON.readTree("{\"login\":\"someone\"}")))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("没有邮箱的账号也能正常建号（GitHub 用户可以隐藏邮箱）")
    void accountWithoutEmailIsAllowed() {
        AuthTokens issued = oauth.loginOrRegister(OAuthProvider.GITHUB,
                profile(uniqueSubject(), "noemail-" + uniqueSubject(), null));

        assertThat(issued.user().email()).isNull();
        assertThat(users.findById(Long.parseLong(issued.user().id()))).isPresent();
    }
}
