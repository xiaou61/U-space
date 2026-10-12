package io.github.xiaou61.ai.internal.support;

import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 按用户的**出站调用**限流（固定窗口）。
 *
 * <p>BYOK 不代表可以无限打我们的出口：Key 是用户的，但请求是从我们的服务器发出的，
 * 被打的上游会把我们的出口一起限掉。读缓存、读列表都不计入——它们不产生任何上游调用。
 *
 * <p>口径由调用方给（次数上限与动作名），因为"精简一次"和"对话一轮"的代价差一个量级：
 * 用同一组数字会让便宜的调用被贵的拖累，或反过来让贵的太容易撞线。
 *
 * <p>ponytail: 进程内固定窗口，单实例部署够用；**天花板**是重启清零、多实例各算各的。
 * 需要跨实例或跨重启保留时换集中式（Redis 或数据库计数），本项不做。
 *
 * <p>原来只在精简里用（叫 {@code SummaryRateLimiter}）；对话要同一套机制，于是提升到本包并
 * 参数化——复制一份"固定窗口"出来让两边各自漂移，是明显更坏的选项。
 */
@Component
public class UserRateLimiter {

    private final Map<Long, Window> windows = new ConcurrentHashMap<>();

    /**
     * 记一次调用并检查是否超限；超限抛 42900。
     *
     * @param action   动作名，只用在错误消息里（"发送消息的每分钟次数已达上限"）
     * @param perMinute 每分钟上限
     * @param perDay    每天上限
     */
    public void checkAndCount(long userId, String action, int perMinute, int perDay) {
        Window window = windows.computeIfAbsent(userId, key -> new Window());
        synchronized (window) {
            Instant now = Instant.now();
            window.roll(now);
            if (window.minuteCount >= perMinute) {
                throw tooMany(action, "每分钟");
            }
            if (window.dayCount >= perDay) {
                throw tooMany(action, "每天");
            }
            window.minuteCount++;
            window.dayCount++;
        }
    }

    private static BizException tooMany(String action, String unit) {
        return new BizException(ErrorCode.TOO_MANY_REQUESTS, action + "的" + unit + "次数已达上限，请稍后再试");
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
