package io.github.xiaou61.account.internal.oauth;

import tools.jackson.databind.JsonNode;

/**
 * 从平台用户信息里归一化出来的最小事实。
 *
 * <p><b>只有三样</b>，而且只有 {@code providerUserId} 是必需的：它决定"这是不是同一个人"。
 * 昵称与邮箱都只用于首次建号的展示与预填，**不参与身份判定**——两者在平台上都会变，
 * 拿它们做唯一键等于把账号绑在一个会漂移的值上。
 *
 * @param providerUserId 平台内稳定 id（GitHub 的数值 id、Gitee 的 id）
 * @param nickname       显示名，可能为空
 * @param email          邮箱，可能为空（用户没公开、或平台不给）
 */
public record OAuthProfile(String providerUserId, String nickname, String email) {

    /**
     * 按平台字段解析。
     *
     * <p>两个平台的字段名恰好都是 {@code id} / {@code name|login} / {@code email}——
     * Gitee API v5 明确参照 GitHub 设计。**但仍不假设字段一定存在**：GitHub 的 {@code email}
     * 在用户没公开邮箱时是 {@code null}，而 {@code name} 也可能为空（只有 {@code login}）。
     *
     * @throws io.github.xiaou61.platform.BizException 缺稳定 id。**不回落到昵称或邮箱建号**：
     *                                                那会让两个不同的人撞成同一个账号
     */
    public static OAuthProfile from(JsonNode body) {
        if (body == null || !body.hasNonNull("id")) {
            throw new io.github.xiaou61.platform.BizException(
                    io.github.xiaou61.platform.ErrorCode.UNAUTHENTICATED,
                    "无法确认第三方账号身份，请重试");
        }
        String id = body.get("id").asText();
        String nickname = firstNonBlank(text(body, "name"), text(body, "login"));
        String email = text(body, "email");
        return new OAuthProfile(id, nickname, email);
    }

    private static String text(JsonNode body, String field) {
        JsonNode node = body.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
