package io.github.xiaou61.account.internal.oauth;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 支持第三方登录的平台（WORK-037）。
 *
 * <p><b>本枚举是平台差异的唯一落点</b>：新增一个平台只在这里加一个取值 + 一段配置，
 * 登录主链路、表结构、前端都不动（REQ-009）。这一条是刻意的——把差异散到各处，
 * 第三个平台就会变成"改四个模块"。
 *
 * <p>目前只有 GitHub 与 Gitee：QQ 与微博要求网站域名已完成 ICP 备案，微信要求企业主体。
 * 三者都不在本期范围，加进来时这里的端点与 scope 要重新核对。
 *
 * <p><b>为什么两个平台的字段名恰好一样</b>：Gitee 的 API v5 明确参照 GitHub 设计，
 * `id` / `login` / `name` / `email` 同名。这不是巧合，但不该被依赖——解析时仍按
 * "取不到就是没有"处理，而不是假设字段一定存在。
 */
public enum OAuthProvider {

    GITHUB("github", "GitHub",
            "https://github.com/login/oauth/authorize",
            "https://github.com/login/oauth/access_token",
            "https://api.github.com/user",
            // 只读资料 + 读邮箱：日志里不该出现能改用户仓库的授权
            "read:user user:email"),

    GITEE("gitee", "Gitee",
            "https://gitee.com/oauth/authorize",
            "https://gitee.com/oauth/token",
            "https://gitee.com/api/v5/user",
            "user_info");

    private final String key;
    private final String label;
    private final String authorizeUrl;
    private final String tokenUrl;
    private final String userUrl;
    private final String scope;

    OAuthProvider(String key, String label, String authorizeUrl, String tokenUrl, String userUrl, String scope) {
        this.key = key;
        this.label = label;
        this.authorizeUrl = authorizeUrl;
        this.tokenUrl = tokenUrl;
        this.userUrl = userUrl;
        this.scope = scope;
    }

    /** 路径与配置里的取值（小写）。 */
    public String key() {
        return key;
    }

    public String label() {
        return label;
    }

    public String tokenUrl() {
        return tokenUrl;
    }

    public String userUrl() {
        return userUrl;
    }

    public String scope() {
        return scope;
    }

    /**
     * 拼授权页地址。
     *
     * <p>{@code redirect_uri} 由服务端从配置拼出，**不接受客户端传入**：一旦接受，
     * 这个端点就成了开放重定向的跳板。
     */
    public String authorizeUrl(String clientId, String redirectUri, String state) {
        return authorizeUrl
                + "?client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&response_type=code"
                + "&scope=" + encode(scope)
                + "&state=" + encode(state);
    }

    /** 认不出就返回空，让调用方按"平台不存在"处理，而不是猜一个默认平台。 */
    public static OAuthProvider ofKey(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (OAuthProvider provider : values()) {
            if (provider.key.equals(normalized)) {
                return provider;
            }
        }
        return null;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
