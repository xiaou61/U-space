package io.github.xiaou61.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * MDC 传播（WORK-022 AC-016）。
 *
 * <p>这条测的是"表里有 trace、日志里也有"这句话的前半截：线程池的线程能拿到请求线程的
 * MDC。没有它，同一次调用的日志行与记录行对不上——那就等于白记。
 *
 * <p>两个必须成立的点：**复制**（不是共享同一个 map）、**跑完恢复**（线程池的线程会被复用，
 * 不恢复就把 A 请求的标识带进 B 请求）。
 */
class MdcTaskDecoratorTest {

    @AfterEach
    void clear() {
        MDC.clear();
    }

    @Test
    @DisplayName("MDC 能带进异步任务，且任务结束后不污染调用线程")
    void mdcSurvivesExecutorHop() {
        MDC.put("traceId", "http-trace-1");
        MDC.put("aiTraceId", "ai-trace-1");

        AtomicReference<String> seenTrace = new AtomicReference<>();
        AtomicReference<String> seenAi = new AtomicReference<>();
        Runnable decorated = new MdcTaskDecorator().decorate(() -> {
            seenTrace.set(MDC.get("traceId"));
            seenAi.set(MDC.get("aiTraceId"));
            // 模拟任务自己往里写：不该漏到调用线程
            MDC.put("traceId", "modified-inside");
        });

        // 调用线程先清空，证明任务里的值确实来自捕获而不是巧合
        MDC.clear();
        decorated.run();

        assertThat(seenTrace.get()).isEqualTo("http-trace-1");
        assertThat(seenAi.get()).isEqualTo("ai-trace-1");
        // 跑完之后调用线程仍是空的（恢复原值，原值是空）
        assertThat(MDC.get("traceId")).isNull();
    }

    @Test
    @DisplayName("没有 MDC 时任务里也是空的，不继承上一条任务的残留")
    void emptyContextStaysEmpty() {
        AtomicReference<String> seen = new AtomicReference<>("untouched");
        Runnable decorated = new MdcTaskDecorator().decorate(() -> seen.set(MDC.get("traceId")));

        MDC.put("traceId", "leftover");
        MDC.clear();
        decorated.run();

        assertThat(seen.get()).isNull();
    }
}
