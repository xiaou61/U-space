package io.github.xiaou61.ai.internal.telemetry;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 有界写队列 + 单写线程（WORK-022 T03）。
 *
 * <p>三条硬纪律：
 * <ol>
 *   <li><b>调用方线程零 IO</b>：`offer` 只做入队，满了立刻返回 false。</li>
 *   <li><b>满了就丢并计数</b>：统计系统不许让用户等。丢弃计数会定时打日志——持续丢弃
 *       说明队列容量与流量不匹配，那是要调参的信号。</li>
 *   <li><b>异常吞掉继续</b>：一条写失败不该停掉整个写入循环；也**不重试**——库故障时
 *       重试只会把队列撑爆，把"统计挂了"升级成"应用挂了"。</li>
 * </ol>
 */
@Component
class RunWriteQueue implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(RunWriteQueue.class);

    private final BlockingQueue<RunPayload> queue;
    private final TelemetryProperties properties;
    private final RunSink sink;
    private final AtomicLong dropped = new AtomicLong();
    private final AtomicLong writeFailures = new AtomicLong();
    private volatile boolean running = true;
    private Thread writer;

    RunWriteQueue(RunSink sink, TelemetryProperties properties) {
        this.sink = sink;
        this.properties = properties;
        this.queue = new ArrayBlockingQueue<>(Math.max(properties.getQueueCapacity(), 16));
    }

    /** 入队。满返回 false——**调用方不该重试，也不该等**。 */
    boolean offer(RunPayload payload) {
        if (!running) {
            return false;
        }
        boolean accepted = queue.offer(payload);
        if (!accepted) {
            dropped.incrementAndGet();
        }
        return accepted;
    }

    long dropped() {
        return dropped.get();
    }

    long writeFailures() {
        return writeFailures.get();
    }

    @Override
    public void afterPropertiesSet() {
        start();
    }

    void start() {
        writer = new Thread(this::loop, "ai-telemetry-writer");
        writer.setDaemon(true);
        writer.start();
    }

    @Override
    public void destroy() {
        stop();
    }

    void stop() {
        running = false;
        if (writer != null) {
            writer.interrupt();
        }
    }

    /**
     * 单线程消费。
     *
     * <p>攒批：一次 poll 到一条后再连着取几条（最多 batchSize），减少数据库往返。
     * **Begin 不攒**——它要尽快落库，否则"运行中"的可见性就没了。
     */
    private void loop() {
        List<RunPayload> batch = new ArrayList<>(Math.max(properties.getBatchSize(), 8));
        while (running || !queue.isEmpty()) {
            try {
                RunPayload first = queue.poll(200, TimeUnit.MILLISECONDS);
                if (first == null) {
                    continue;
                }
                write(first);
                batch.clear();
                queue.drainTo(batch, Math.max(properties.getBatchSize() - 1, 1));
                for (RunPayload payload : batch) {
                    write(payload);
                }
                reportDropsIfAny();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            } catch (RuntimeException unexpected) {
                // 循环本身不该因为某条的任务炸掉；这一层是最后的安全网
                log.warn("ai.telemetry.loop-failed", unexpected);
            }
        }
    }

    private void write(RunPayload payload) {
        try {
            sink.accept(payload);
        } catch (RuntimeException failure) {
            writeFailures.incrementAndGet();
            log.warn("ai.telemetry.write-failed kind={} traceId={} cause={}",
                    payload.getClass().getSimpleName(),
                    payload instanceof RunPayload.RunBegin b ? b.run().traceId()
                            : ((RunPayload.RunFinish) payload).run().traceId(),
                    failure.getClass().getSimpleName());
        }
    }

    /** 每处理完一批报一次：持续丢弃时这条日志是唯一的信号。 */
    private void reportDropsIfAny() {
        long total = dropped.get();
        if (total > 0) {
            log.info("ai.telemetry.dropped total={} failures={}", total, writeFailures.get());
        }
    }
}
