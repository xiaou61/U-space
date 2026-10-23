package io.github.xiaou61.account.internal.oauth;

/**
 * 一次授权往返的用途。
 *
 * <p>为什么要区分：登录和绑定走的是同一个回调端点，但**权限语义完全不同**——
 * 登录是无状态建会话，绑定必须是"已登录的 A 把 B 绑到自己身上"。如果回调不区分，
 * 一个登录 state 就能被当成绑定用，或反过来。
 *
 * <p>取值随 state 存在服务端，**不接受客户端传入**。
 */
public enum OAuthPurpose {
    /** 匿名用户建立会话。 */
    LOGIN,
    /** 已登录用户把第三方账号绑到自己身上。 */
    BIND
}
