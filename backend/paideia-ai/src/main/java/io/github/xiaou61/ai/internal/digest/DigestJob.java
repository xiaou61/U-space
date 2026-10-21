package io.github.xiaou61.ai.internal.digest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 每日速递的定时任务（WORK-027）。
 *
 * <p>用 `fixedDelay` 而不是 cron：与既有三个定时任务同一理由——它保证"上一次跑完再过 24 小时"，
 * 上游慢的时候不会叠起来。
 *
 * <p>**异常必须吞掉**：`@Scheduled` 抛出的异常只会让 Spring 记一条错误，下一轮照旧；
 * 但让它抛出去会让"今天抓失败了"这件事淹没在堆栈里。
 *
 * <p>**刻意不加 `@Component`**：只在开关打开时由 {@link DigestConfiguration} 注册。
 */
class DigestJob {

    private static final Logger log = LoggerFactory.getLogger(DigestJob.class);

    /** 首次执行前的等待：避开应用启动时与其它初始化抢资源。 */
    private static final long INITIAL_DELAY_MS = 5 * 60 * 1000L;

    /** 24 小时。 */
    private static final long DAILY_MS = 24 * 60 * 60 * 1000L;

    private final DigestCollectionService service;

    DigestJob(DigestCollectionService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = DAILY_MS, initialDelay = INITIAL_DELAY_MS)
    void collectDaily() {
        try {
            DigestCollectionService.RunResult result = service.runOnce();
            if (result.skipReason() != null) {
                log.info("速递跳过：{}", result.skipReason());
            }
        } catch (RuntimeException exception) {
            log.error("速递任务异常", exception);
        }
    }
}
