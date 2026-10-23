package io.github.xiaou61.account.internal.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.xiaou61.account.internal.user.User;
import io.github.xiaou61.account.internal.user.UserMapper;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 从平台昵称生成站内用户名（WORK-037 REQ-002）。
 *
 * <p>这里的每一条都对应一个会真实发生的情况：GitHub 昵称里可以有空格与表情、
 * 中文昵称、纯符号昵称、以及最常见的——同名用户。
 */
class UsernameAllocatorTest {

    private final UserMapper users = mock(UserMapper.class);

    @Test
    @DisplayName("不冲突时直接用清洗后的昵称")
    void usesSanitizedNicknameWhenFree() {
        when(users.findByUsername(anyString())).thenReturn(Optional.empty());
        UsernameAllocator allocator = new UsernameAllocator(users);

        assertThat(allocator.allocate("Zhang San")).isEqualTo("zhangsan");
    }

    @Test
    @DisplayName("冲突时加数字后缀，从 -2 开始")
    void appendsSuffixOnCollision() {
        // "zhangsan" 与 "zhangsan-2" 都被占，第三个应该拿到 -3
        when(users.findByUsername("zhangsan")).thenReturn(Optional.of(existing()));
        when(users.findByUsername("zhangsan-2")).thenReturn(Optional.of(existing()));
        when(users.findByUsername("zhangsan-3")).thenReturn(Optional.empty());
        UsernameAllocator allocator = new UsernameAllocator(users);

        assertThat(allocator.allocate("zhangsan")).isEqualTo("zhangsan-3");
    }

    @Test
    @DisplayName("连续 50 次都撞名时失败，不静默兜底成别的名字")
    void failsAfterTooManyCollisions() {
        when(users.findByUsername(anyString())).thenReturn(Optional.of(existing()));
        UsernameAllocator allocator = new UsernameAllocator(users);

        assertThatThrownBy(() -> allocator.allocate("popular"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("无法生成");
    }

    @Test
    @DisplayName("空格、表情、点号都被去掉；大小写归一")
    void stripsCharactersTheRegisterFormRejects() {
        // 注册接口的规则是 ^[\p{L}\p{N}_-]+$：生成的名字必须能通过同一套校验，
        // 否则用户改资料时会被自己的规则挡住
        assertThat(UsernameAllocator.sanitize("Li.Lei 🚀")).isEqualTo("lilei");
        assertThat(UsernameAllocator.sanitize("a b_c-d")).isEqualTo("ab_c-d");
    }

    @Test
    @DisplayName("中文昵称保留——注册接口本来就允许 Unicode 字母")
    void keepsChineseNicknames() {
        assertThat(UsernameAllocator.sanitize("张伟")).isEqualTo("张伟");
    }

    @Test
    @DisplayName("清洗后为空才退到 fallback；两三个字的名字照常保留")
    void fallsBackOnlyWhenEmpty() {
        assertThat(UsernameAllocator.sanitize(null)).isEqualTo("user");
        assertThat(UsernameAllocator.sanitize("")).isEqualTo("user");
        assertThat(UsernameAllocator.sanitize("  !!  ")).isEqualTo("user");
        // 两个字符不该被丢掉：中文姓名两个字是最常见的情况
        assertThat(UsernameAllocator.sanitize("ab")).isEqualTo("ab");
    }

    @Test
    @DisplayName("加后缀后仍不超过 64 字符上限")
    void suffixNeverExceedsColumnLimit() {
        String longName = "x".repeat(80);
        String cleaned = UsernameAllocator.sanitize(longName);
        String suffixed = UsernameAllocator.withSuffix(cleaned, 123);

        assertThat(cleaned.length()).isLessThanOrEqualTo(64);
        assertThat(suffixed.length()).isLessThanOrEqualTo(64);
        assertThat(suffixed).endsWith("-123");
    }

    private static User existing() {
        User user = new User();
        user.setId(1L);
        user.setUsername("taken");
        return user;
    }
}
