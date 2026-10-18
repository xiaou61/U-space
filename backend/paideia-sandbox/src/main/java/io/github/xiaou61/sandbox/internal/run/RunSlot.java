package io.github.xiaou61.sandbox.internal.run;

import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * 全局并发闸（WORK-015）。
 *
 * <p><b>为什么是"拒绝"而不是"排队"</b>：请求已经在一次 HTTP 调用里等着了，排队只会让用户看到
 * 一个没有上限的转圈；而容器启动本身是秒级的。直接告诉他"现在忙"更诚实。
 *
 * <p>闸的粒度是**进程级**：单实例部署下它就是全局上限。将来多实例时每台各有 2 个，
 * 真正的全局上限要靠外部配额——那时再看要不要换。
 */
@Component
public class RunSlot {

    private final Semaphore slots;

    public RunSlot(SandboxProperties properties) {
        this.slots = new Semaphore(Math.max(1, properties.getMaxConcurrency()), true);
    }

    /** 拿不到就抛 429；拿到必须 close。 */
    public Slot acquire() {
        boolean acquired;
        try {
            acquired = slots.tryAcquire(2, TimeUnit.SECONDS);
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            acquired = false;
        }
        if (!acquired) {
            throw new BizException(ErrorCode.TOO_MANY_REQUESTS, "现在运行的人有点多，稍后再试");
        }
        return new Slot();
    }

    public final class Slot implements AutoCloseable {
        private boolean released;

        @Override
        public void close() {
            if (!released) {
                released = true;
                slots.release();
            }
        }
    }
}
