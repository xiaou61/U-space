package io.github.xiaou61.account.internal.oauth;

/**
 * 一次授权往返的服务端记录。
 *
 * @param provider 发起时选的平台。回调必须与它一致——不一致说明 state 被挪用到另一个平台
 * @param purpose  登录还是绑定
 * @param userId   绑定时是发起人的账号 id；登录时为 null（那时还没有账号可谈）
 * @param desktop  发起方是不是桌面壳。**由服务端记录而不是回调时再问**：回调是浏览器
 *                 导航过来的，没有任何办法在那时判断"最初是谁点的登录"
 */
public record OAuthState(OAuthProvider provider, OAuthPurpose purpose, Long userId, boolean desktop) {
}
