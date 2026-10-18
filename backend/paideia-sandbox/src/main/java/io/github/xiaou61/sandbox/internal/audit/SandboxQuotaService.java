package io.github.xiaou61.sandbox.internal.audit;

import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import io.github.xiaou61.sandbox.internal.run.CodeRun;
import io.github.xiaou61.sandbox.internal.run.SandboxProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HexFormat;
import org.springframework.stereotype.Service;

/**
 * 配额与审计（WORK-015）。
 *
 * <p><b>配额按 UTC 日切</b>：项目里时间一律按 UTC 存与算（V6 起的约定），这里沿用；
 * 不为了"看起来像本地的一天"引入时区，那会让跨时区部署的计数不一致。
 *
 * <p><b>被拒绝的也写审计</b>，但不计入次数。写审计是为了回答"他为什么被挡"——
 * 如果只在成功时写，用户说"我明明没跑几次"时就无从对账。
 */
@Service
public class SandboxQuotaService {

    private final CodeRunMapper mapper;
    private final SandboxProperties properties;

    SandboxQuotaService(CodeRunMapper mapper, SandboxProperties properties) {
        this.mapper = mapper;
        this.properties = properties;
    }

    /** 今天还剩几次。 */
    public int remaining(long userId) {
        return Math.max(0, properties.getDailyLimit() - usedToday(userId));
    }

    public int usedToday(long userId) {
        return mapper.countSince(userId, startOfUtcDay());
    }

    /** 超了抛 429。**在真正执行之前调用**，避免先跑再拒绝。 */
    public void requireQuota(long userId) {
        if (usedToday(userId) >= properties.getDailyLimit()) {
            throw new BizException(ErrorCode.TOO_MANY_REQUESTS,
                    "今天的运行次数已用完（每天 " + properties.getDailyLimit() + " 次），明天再来");
        }
    }

    /** 写一条审计。`status` 用 {@link CodeRun.Status} 的名字，存进去就是稳定值。 */
    public void record(long userId, Long entryId, String caseId, String language,
                       CodeRun.Status status, Integer exitCode, long durationMs,
                       int outputBytes, boolean truncated, String code) {
        mapper.insert(userId, entryId, caseId, language, status.name(), exitCode,
                durationMs, outputBytes, truncated, sha256(code), code.length(), Instant.now());
    }

    /** 把运行结果归到审计状态。判错与"跑起来了但程序返回非 0"是两回事。 */
    public static CodeRun.Status statusOf(CodeRun.Result result) {
        if (result.timedOut() && result.outOfMemory()) {
            return CodeRun.Status.OUT_OF_MEMORY;
        }
        if (result.timedOut()) {
            return CodeRun.Status.TIMEOUT;
        }
        if (result.outOfMemory()) {
            return CodeRun.Status.OUT_OF_MEMORY;
        }
        return result.exitCode() == 0 ? CodeRun.Status.OK : CodeRun.Status.RUNTIME_ERROR;
    }

    static Instant startOfUtcDay() {
        return LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException cause) {
            // SHA-256 是 JDK 必备算法，走到这里说明运行环境坏了
            throw new IllegalStateException("缺少 SHA-256", cause);
        }
    }
}
