package io.github.xiaou61.web;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

/**
 * 把 MDC 带进异步任务（WORK-022 T05）。
 *
 * <p><b>为什么必须有它</b>：对话的流式消费跑在 `applicationTaskExecutor` 里，而
 * `TraceIdFilter` 在请求线程结束时就把 MDC 清掉了。没有这个装饰器，同一轮调用的
 * 日志行与记录行对不上——表里有 trace_id、日志里没有，等于白记。
 *
 * <p>两条纪律：<b>复制而不是共享</b>（`getCopyOfContextMap`），<b>跑完恢复原值</b>。
 * 线程池的线程会被复用，不恢复就等于把 A 请求的标识带进 B 请求。
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> captured = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            if (captured == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(captured);
            }
            try {
                runnable.run();
            } finally {
                if (previous == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previous);
                }
            }
        };
    }
}
