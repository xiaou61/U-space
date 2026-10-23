package io.github.xiaou61.account.internal.oauth;

import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * 从平台昵称生成一个不冲突的站内用户名。
 *
 * <p><b>为什么必须生成而不是让用户填</b>：授权的价值就在"不用先填表单"。停下来让他起名，
 * 第三方登录就退化成了"注册第一步"。
 *
 * <p><b>冲突用数字后缀</b>（用户裁决 Q3）。**不截断、不随机化**：加后缀是用户能认出来的
 * 结果，随机串会让人以为账号被盗。上限 50 次，超过就失败——静默兜底成一个别的名字
 * 比失败更糟。
 *
 * <p><b>清洗规则跟随注册接口</b>：`^[\p{L}\p{N}_-]+$`，长度 3–64。不一致的话，
 * 生成出来的用户名会在"用户之后想改资料"时被自己的校验挡住。
 */
@Component
class UsernameAllocator {

    private static final int MAX_LENGTH = 64;
    private static final int MAX_ATTEMPTS = 50;
    private static final String FALLBACK_BASE = "user";

    private final io.github.xiaou61.account.internal.user.UserMapper users;

    UsernameAllocator(io.github.xiaou61.account.internal.user.UserMapper users) {
        this.users = users;
    }

    /**
     * 生成一个当前未被占用的用户名。
     *
     * @param preferred 平台给的昵称，可为空
     * @throws IllegalStateException 连续 50 次都撞名。真实场景下不可能发生；
     *                               真发生了说明有人在批量占名字，失败比随机兜底安全
     */
    String allocate(String preferred) {
        String base = sanitize(preferred);
        if (isFree(base)) {
            return base;
        }
        for (int suffix = 2; suffix <= MAX_ATTEMPTS; suffix += 1) {
            String candidate = withSuffix(base, suffix);
            if (isFree(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("无法生成可用的用户名：" + base);
    }

    /**
     * 只留字母/数字/下划线/连字符；清洗后为空时退到 fallback。
     *
     * <p><b>不套用注册接口的 {@code min = 3}</b>：那个下限对中文名过严（"张伟"只有两个字符，
     * 而这在中文里是最常见的姓名长度）。本项目没有改用户名的入口，生成出来的短名字不会被
     * 哪条规则挡住。真正要守的是字符集与列宽上限——超了这两个才是硬错。
     */
    static String sanitize(String preferred) {
        String cleaned = preferred == null
                ? ""
                : preferred.replaceAll("[^\\p{L}\\p{N}_-]", "").trim().toLowerCase(Locale.ROOT);
        if (cleaned.length() > MAX_LENGTH - 4) {
            // 留 4 个字符给 "-99" 这类后缀，避免"截断后加后缀又超长"
            cleaned = cleaned.substring(0, MAX_LENGTH - 4);
        }
        if (cleaned.isEmpty()) {
            return FALLBACK_BASE;
        }
        return cleaned;
    }

    /** 加后缀时也要保证不超长：从尾部截掉，因为前缀才是用户认得的部分。 */
    static String withSuffix(String base, int suffix) {
        String tail = "-" + suffix;
        int room = MAX_LENGTH - tail.length();
        String head = base.length() > room ? base.substring(0, room) : base;
        return head + tail;
    }

    private boolean isFree(String username) {
        return users.findByUsername(username).isEmpty();
    }
}
