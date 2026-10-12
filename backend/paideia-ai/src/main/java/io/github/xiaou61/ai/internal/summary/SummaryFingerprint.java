package io.github.xiaou61.ai.internal.summary;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 正文指纹：正文的 SHA-256（十六进制）。
 *
 * <p>它是缓存键的一部分，作用是让"正文改了"这件事**自动**使旧结果失效。用哈希而不是
 * 条目表的 `updated_at`：后者依赖"任何一次正文改动都会刷新时间戳"这个隐式前提，
 * 而哈希把"内容没变就不该重新生成"写成显式事实，成本可以忽略。
 */
final class SummaryFingerprint {

    private SummaryFingerprint() {
    }

    static String of(String body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((body == null ? "" : body).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            // SHA-256 是 JDK 必须提供的算法，走到这里说明运行环境坏了
            throw new IllegalStateException("当前 JVM 没有 SHA-256", exception);
        }
    }
}
