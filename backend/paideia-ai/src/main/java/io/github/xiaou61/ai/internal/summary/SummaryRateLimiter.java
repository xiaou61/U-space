package io.github.xiaou61.ai.internal.summary;

import io.github.xiaou61.ai.internal.credential.AiProperties;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 按用户的生成限流（固定窗口）。
 *
 * <p>BYOK 不代表可以无限打我们的出口：Key 是用户的，但请求是从我们的服务器发出的，
 * 被打的上游会把我们的出口一起限掉。读缓存不计入——它不产生任何上游调用。
 *
 * <p>ponytail: 进程内固定窗口，单实例部署够用；**天花板**是重启清零、多实例各算各的。
 * 需要跨实例或跨重启保留时换集中式（Redis 或数据库计数），本项不做。
 */
@Component
class SummaryRateLimiter {

    private final AiProperties properties;
    private final Map<Long, Window> windows = new ConcurrentHashMap<>();

    SummaryRateLimiter(AiProperties properties) {
        this.properties = properties;
    }

    /** 记一次生成并检查是否超限；超限抛 42900。 */
    void checkAndCount(long userId) {
        Window window = windows.computeIfAbsent(userId, key -> new Window());
        synchronized (window) {
            Instant now = Instant.now();
            window.roll(now);
            if (window.minuteCount >= properties.getPerMinuteLimit()) {
                throw tooMany("每分钟");
            }
            if (window.dayCount >= properties.getPerDayLimit()) {
                throw tooMany("每天");
            }
            window.minuteCount++;
            window.dayCount++;
        }
    }

    private static BizException tooMany(String unit) {
        return new BizException(ErrorCode.TOO_MANY_REQUESTS, "触发精简的" + unit + "次数已达上限，请稍后再试");
    }

    private static final class Window {

        private Instant minuteStartedAt = Instant.EPOCH;
        private Instant dayStartedAt = Instant.EPOCH;
        private int minuteCount;
        private int dayCount;

        void roll(Instant now) {
            if (minuteStartedAt.plusSeconds(60).isBefore(now)) {
                minuteStartedAt = now;
                minuteCount = 0;
            }
            if (dayStartedAt.plusSeconds(86400).isBefore(now)) {
                dayStartedAt = now;
                dayCount = 0;
            }
        }
    }
}
