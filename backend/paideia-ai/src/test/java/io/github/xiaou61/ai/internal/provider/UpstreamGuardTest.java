package io.github.xiaou61.ai.internal.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xiaou61.platform.BizException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * SSRF 校验的行为。
 *
 * <p>这是信任边界的测试，用**字面 IP** 而不是域名，所以不依赖网络：回环、私网、元数据地址
 * 都能直接构造出来。
 */
class UpstreamGuardTest {

    private final UpstreamGuard guard = new UpstreamGuard();

    @ParameterizedTest
    @ValueSource(strings = {
            "http://api.example.com/v1",            // 非 https
            "https://",                             // 没有主机
            "https://user:pass@api.example.com/v1", // 带凭据
            "https://127.0.0.1/v1",                 // 回环
            "https://localhost:8081/v1",            // 解析到回环
            "https://[::1]/v1",                     // IPv6 回环
            "https://10.0.0.5/v1",                  // 私网 A
            "https://172.16.3.4/v1",                // 私网 B
            "https://192.168.1.10/v1",              // 私网 C
            "https://169.254.169.254/latest/meta-data", // 云元数据
            "https://100.64.0.1/v1",                // 运营商级 NAT
            "https://[fc00::1]/v1",                 // IPv6 唯一本地地址
            "https://[::ffff:127.0.0.1]/v1",        // IPv4 映射的回环
            "https://[::127.0.0.1]/v1",             // IPv4-compatible 的回环（JDK 不转成 Inet4Address，易漏判）
            "https://[::169.254.169.254]/v1",       // IPv4-compatible 的云元数据
            "不是地址",
    })
    @DisplayName("拒掉一切非公网 https 目标")
    void rejectsNonPublicTargets(String baseUrl) {
        assertThatThrownBy(() -> guard.assertAllowed(baseUrl))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("baseUrl 不被允许");
    }

    @Test
    @DisplayName("公网 https 地址放行")
    void allowsPublicHttps() {
        // 用字面公网 IP，避免依赖 DNS；也顺带证明"不是拦所有 https"
        assertThatCode(() -> guard.assertAllowed("https://1.1.1.1/v1")).doesNotThrowAnyException();
        assertThatCode(() -> guard.assertAllowed("https://8.8.8.8:8443/v1")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("错误信息不回显解析到的内网地址")
    void doesNotLeakInternalAddress() {
        // 把内网地址回给用户等于送他一个内网探测器；这里只说明"解析到非公网地址"
        assertThatThrownBy(() -> guard.assertAllowed("https://10.1.2.3/v1"))
                .isInstanceOf(BizException.class)
                .satisfies(exception -> assertThat(exception.getMessage()).doesNotContain("10.1.2.3"));
    }
}
