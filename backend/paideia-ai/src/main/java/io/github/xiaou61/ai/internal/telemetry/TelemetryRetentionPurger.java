package io.github.xiaou61.ai.internal.telemetry;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 保留期清理与僵尸行标记（WORK-022 T09）。
 *
 * <p>两件事，都是"没人做就会一直烂在那里"的：
 * <ol>
 *   <li><b>过期删除</b>：按 `started_at` 分批删（先删步骤再删轮次），一次 500 行，
 *       循环到删空为止。不锁全表——本轮删不完下次接着删。</li>
 *   <li><b>僵尸行标记</b>：一直停在 `running` 的（进程崩溃、线程卡死）。不标它们，
 *       统计里"进行中"会越来越多，而那个数字是排查的第一眼。</li>
 * </ol>
 *
 * <p>用 `fixedDelay` 而不是 cron：它保证"上一次跑完再过一小时"，库慢的时候不会叠起来。
 */
@Component
public class TelemetryRetentionPurger {

    private static final Logger log = LoggerFactory.getLogger(TelemetryRetentionPurger.class);

    /** 一批删多少。太小则慢，太大则长时间持锁。 */
    private static final int BATCH = 500;

    private final AiRunMapper runs;
    private final AiRunSpanMapper spans;
    private final TelemetryProperties properties;

    TelemetryRetentionPurger(AiRunMapper runs, AiRunSpanMapper spans, TelemetryProperties properties) {
        this.runs = runs;
        this.spans = spans;
        this.properties = properties;
    }

    @Scheduled(fixedDelay = 3600_000L)
    public void purge() {
        if (!properties.isEnabled()) {
            return;
        }
        markStuck();
        deleteExpired();
    }

    private void markStuck() {
        Instant threshold = Instant.now().minus(properties.getStuckAfterHours(), ChronoUnit.HOURS);
        int marked = runs.markStuck(threshold, Instant.now());
        if (marked > 0) {
            log.warn("ai.telemetry.stuck-marked rows={} before={}", marked, threshold);
        }
    }

    private void deleteExpired() {
        Instant before = Instant.now().minus(properties.getRetentionDays(), ChronoUnit.DAYS);
        int total = 0;
        while (true) {
            List<String> expired = runs.findExpired(before, BATCH);
            if (expired.isEmpty()) {
                break;
            }
            // 先删步骤、再删轮次：本项没有外键，顺序由应用保证
            spans.deleteByTraceIds(expired);
            runs.deleteByTraceIds(expired);
            total += expired.size();
            if (expired.size() < BATCH) {
                break;
            }
        }
        if (total > 0) {
            log.info("ai.telemetry.purged rows={} before={}", total, before);
        }
    }
}
