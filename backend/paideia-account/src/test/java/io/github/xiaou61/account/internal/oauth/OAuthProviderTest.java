package io.github.xiaou61.account.internal.oauth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 平台差异集中处（WORK-037 REQ-009）。
 *
 * <p>这层测试的价值不在"代码对不对"，而在**把两个平台的协议细节钉住**：端点地址、
 * scope、参数名一旦写错，症状是"点了没反应"或平台回 400，而那时人只会去看浏览器跳转，
 * 不会想到是枚举里的一个字符串。
 */
class OAuthProviderTest {

    @Test
    @DisplayName("GitHub 授权地址含正确 host、scope、state，且参数都做了 URL 编码")
    void buildsGithubAuthorizeUrl() {
        String url = OAuthProvider.GITHUB.authorizeUrl(
                "Iv1.abc", "http://127.0.0.1:8081/api/v1/auth/oauth/github/callback", "state-xyz");

        assertThat(url).startsWith("https://github.com/login/oauth/authorize?");
        assertThat(url).contains("client_id=Iv1.abc");
        assertThat(url).contains("response_type=code");
        // 回调地址里有冒号与斜杠，必须编码；不编码时部分平台会把它当成额外的参数
        assertThat(url).contains("redirect_uri=http%3A%2F%2F127.0.0.1%3A8081%2Fapi%2Fv1%2Fauth%2Foauth%2Fgithub%2Fcallback");
        assertThat(url).contains("state=state-xyz");
        // 官方 scope：读资料与读邮箱，不给写权限
        assertThat(url).contains("scope=read%3Auser+user%3Aemail");
    }

    @Test
    @DisplayName("Gitee 授权地址与 GitHub 同形，但 host 与 scope 不同")
    void buildsGiteeAuthorizeUrl() {
        String url = OAuthProvider.GITEE.authorizeUrl("cid", "http://example.test/cb", "s1");

        assertThat(url).startsWith("https://gitee.com/oauth/authorize?");
        assertThat(url).contains("scope=user_info");
        assertThat(url).contains("state=s1");
    }

    @Test
    @DisplayName("Gitee 的 token 端点不是 GitHub 那个 /login/oauth/access_token")
    void endpointHostsAreNotShared() {
        assertThat(OAuthProvider.GITHUB.tokenUrl()).isEqualTo("https://github.com/login/oauth/access_token");
        assertThat(OAuthProvider.GITEE.tokenUrl()).isEqualTo("https://gitee.com/oauth/token");
        assertThat(OAuthProvider.GITHUB.userUrl()).isEqualTo("https://api.github.com/user");
        assertThat(OAuthProvider.GITEE.userUrl()).isEqualTo("https://gitee.com/api/v5/user");
    }

    @Test
    @DisplayName("路径里认不出的平台返回 null，不猜默认值")
    void unknownKeyYieldsNull() {
        assertThat(OAuthProvider.ofKey("qq")).isNull();
        assertThat(OAuthProvider.ofKey(null)).isNull();
        assertThat(OAuthProvider.ofKey("  GITHUB  ")).isEqualTo(OAuthProvider.GITHUB);
    }

    @Test
    @DisplayName("缺 clientId 或 secret 即为禁用；两者齐全才可用")
    void disabledUnlessBothCredentialsPresent() {
        OAuthProperties partial = new OAuthProperties(
                Map.of("github", new OAuthProperties.Provider("id-only", null, null)),
                "http://localhost:5173", "http://localhost:8081");
        OAuthProperties complete = new OAuthProperties(
                Map.of("github", new OAuthProperties.Provider("id", "secret", null)),
                "http://localhost:5173", "http://localhost:8081");

        assertThat(partial.enabled(OAuthProvider.GITHUB)).isFalse();
        assertThat(complete.enabled(OAuthProvider.GITHUB)).isTrue();
        // 没配过的平台也必须是禁用，而不是 NPE
        assertThat(complete.enabled(OAuthProvider.GITEE)).isFalse();
    }

    @Test
    @DisplayName("没配 redirect-uri 时从 callbackBaseUrl 拼默认值，且不让双斜杠出现")
    void derivesRedirectUriFromBase() {
        OAuthProperties properties = new OAuthProperties(Map.of(), "", "https://paideia.example/");

        assertThat(properties.redirectUri(OAuthProvider.GITHUB))
                .isEqualTo("https://paideia.example/api/v1/auth/oauth/github/callback");
    }

    @Test
    @DisplayName("平台自己配了 redirect-uri 时以它为准（多环境各配各的）")
    void configuredRedirectUriWins() {
        OAuthProperties properties = new OAuthProperties(
                Map.of("gitee", new OAuthProperties.Provider("id", "secret", "https://a.test/cb")),
                "", "https://ignored.test");

        assertThat(properties.redirectUri(OAuthProvider.GITEE)).isEqualTo("https://a.test/cb");
    }
}
