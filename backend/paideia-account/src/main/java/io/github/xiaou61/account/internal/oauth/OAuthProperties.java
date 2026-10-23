package io.github.xiaou61.account.internal.oauth;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 第三方登录的凭据与回调地址。
 *
 * <p>结构：{@code paideia.oauth.<平台>.client-id / client-secret / redirect-uri}。
 * 用 Map 而不是给每个平台写一段固定字段：加第三个平台时不用改这个类（REQ-009）。
 *
 * <p><b>凭据只从服务器上的 application.yml 或被忽略的本地配置来</b>，仓库里只有占位样例。
 * Secret 一旦进了 Git 历史，删文件也无法补救。
 *
 * <p><b>缺省即禁用</b>：{@code enabled()} 要求 clientId 与 clientSecret 都非空。
 * 这样本地开发不配也能跑，界面上那两个按钮不出现，而不是点了报 500。
 */
@ConfigurationProperties(prefix = "paideia.oauth")
public record OAuthProperties(Map<String, Provider> providers, String webBaseUrl, String callbackBaseUrl) {

    public OAuthProperties {
        providers = providers == null ? Map.of() : new LinkedHashMap<>(providers);
    }

    /** 单个平台的配置。字段名从 kebab-case 映射（client-id → clientId）。 */
    public record Provider(String clientId, String clientSecret, String redirectUri) {

        public boolean enabled() {
            return clientId != null && !clientId.isBlank()
                    && clientSecret != null && !clientSecret.isBlank();
        }
    }

    /** 取某个平台的配置；没配过返回一个"禁用"的空配置，而不是 null。 */
    public Provider provider(OAuthProvider provider) {
        Provider found = providers.get(provider.key().toLowerCase(Locale.ROOT));
        return found == null ? new Provider(null, null, null) : found;
    }

    public boolean enabled(OAuthProvider provider) {
        return provider(provider).enabled();
    }

    /**
     * 回调地址。**必须与平台上登记的一字不差**（GitHub 与 Gitee 都是精确匹配，差一个斜杠就拒）。
     *
     * <p>优先用平台自己配的 redirect-uri（多环境时各配各的）；没配时从
     * {@code callbackBaseUrl} 拼一个默认值。
     */
    public String redirectUri(OAuthProvider provider) {
        Provider config = provider(provider);
        if (config.redirectUri() != null && !config.redirectUri().isBlank()) {
            return config.redirectUri();
        }
        String base = callbackBaseUrl == null || callbackBaseUrl.isBlank() ? "" : stripTrailingSlash(callbackBaseUrl);
        return base + "/api/v1/auth/oauth/" + provider.key() + "/callback";
    }

    private static String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
