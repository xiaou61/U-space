package io.github.xiaou61.ai.internal.essay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 每日抓取面经的定时任务（WORK-025）。
 *
 * <p><b>用 `fixedDelay` 而不是 cron</b>：与既有两个定时任务同一理由——它保证"上一次跑完再过
 * 24 小时"，库慢或上游慢的时候不会叠起来。
 *
 * <p><b>异常必须吞掉</b>：`@Scheduled` 方法抛出的异常只会让 Spring 记一条错误，下一轮照旧；
 * 但让它抛出去会让"今天抓失败了"这件事淹没在堆栈里。这里自己接住并记录。
 *
 * <p>**刻意不加 `@Component`**：这个类只在开关打开时由 {@link EssayConfiguration} 注册，
 * 加了注解会绕过条件装配、每天都空转。
 */
class EssayCollectionJob {

    private static final Logger log = LoggerFactory.getLogger(EssayCollectionJob.class);

    /** 首次执行前的等待：避开应用启动时与其它初始化抢资源。 */
    private static final long INITIAL_DELAY_MS = 5 * 60 * 1000L;

    /** 24 小时，毫秒。 */
    private static final long DAILY_MS = 24 * 60 * 60 * 1000L;

    private final EssayCollectionService service;

    EssayCollectionJob(EssayCollectionService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = DAILY_MS, initialDelay = INITIAL_DELAY_MS)
    void collectDaily() {
        try {
            EssayCollectionService.RunResult result = service.runOnce();
            if (result.skipReason() != null) {
                log.info("八股文抓取跳过：{}", result.skipReason());
            }
        } catch (RuntimeException exception) {
            // 走到这里说明编排本身有 bug（它内部已接住各阶段失败）。记下来但不让调度器炸。
            log.error("八股文抓取任务异常", exception);
        }
    }
}
