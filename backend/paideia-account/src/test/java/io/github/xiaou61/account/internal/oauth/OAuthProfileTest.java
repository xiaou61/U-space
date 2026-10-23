package io.github.xiaou61.account.internal.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xiaou61.platform.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/**
 * 平台用户信息 → 我们认的三个事实（WORK-037）。
 *
 * <p>用真实的 JSON 字面量而不是构造对象：这段代码的全部风险都在**字段形状**上
 * （GitHub 的 email 可以是 null、name 可以缺、id 可能是数字或字符串）。
 */
class OAuthProfileTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    @DisplayName("GitHub 的典型响应：id 是数字，nickname 优先取 name")
    void readsGithubShapedPayload() {
        OAuthProfile profile = OAuthProfile.from(JSON.readTree("""
                {"id": 12345678, "login": "octocat", "name": "The Octocat",
                 "email": "octo@example.test"}
                """));

        assertThat(profile.providerUserId()).isEqualTo("12345678");
        assertThat(profile.nickname()).isEqualTo("The Octocat");
        assertThat(profile.email()).isEqualTo("octo@example.test");
    }

    @Test
    @DisplayName("email 为 null（用户没公开邮箱）时不影响解析")
    void toleratesNullEmail() {
        OAuthProfile profile = OAuthProfile.from(JSON.readTree("""
                {"id": 42, "login": "hidden", "name": null, "email": null}
                """));

        assertThat(profile.providerUserId()).isEqualTo("42");
        assertThat(profile.email()).isNull();
        // name 为 null 时退到 login，否则界面上会是一片空白
        assertThat(profile.nickname()).isEqualTo("hidden");
    }

    @Test
    @DisplayName("完全没有 id 时拒绝，不回落到昵称或邮箱建号")
    void rejectsPayloadWithoutStableId() {
        assertThatThrownBy(() -> OAuthProfile.from(JSON.readTree("""
                {"login": "someone", "email": "someone@example.test"}
                """)))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("无法确认");
    }

    @Test
    @DisplayName("id 为 null 与缺失同等对待")
    void treatsNullIdAsMissing() {
        assertThatThrownBy(() -> OAuthProfile.from(JSON.readTree("""
                {"id": null, "login": "someone"}
                """)))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("两个字段都没有时 nickname 为空，不抛错——昵称不是必需项")
    void allowsEmptyNickname() {
        OAuthProfile profile = OAuthProfile.from(JSON.readTree("""
                {"id": 7}
                """));

        assertThat(profile.providerUserId()).isEqualTo("7");
        assertThat(profile.nickname()).isNull();
    }
}
