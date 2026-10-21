package io.github.xiaou61.ai.internal.digest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 周报定时任务（WORK-027）。
 *
 * <p>与日报同一个开关、独立的一个 Bean：**日报失败不影响周报，周报失败也不影响日报**（AC-013）。
 */
class DigestWeeklyJob {

    private static final Logger log = LoggerFactory.getLogger(DigestWeeklyJob.class);

    /** 首次等待：比日报晚一点，避开启动时一起抢资源。 */
    private static final long INITIAL_DELAY_MS = 10 * 60 * 1000L;

    /** 7 天。 */
    private static final long WEEKLY_MS = 7 * 24 * 60 * 60 * 1000L;

    private final DigestWeeklyService service;

    DigestWeeklyJob(DigestWeeklyService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = WEEKLY_MS, initialDelay = INITIAL_DELAY_MS)
    void generateWeekly() {
        try {
            DigestWeeklyService.WeeklyResult result = service.runWeekly();
            if (result.skipReason() != null) {
                log.info("周报跳过：{}", result.skipReason());
            }
        } catch (RuntimeException exception) {
            log.error("周报任务异常", exception);
        }
    }
}
