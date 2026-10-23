package io.github.xiaou61.sandbox.internal.run;

import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

/**
 * 全局并发闸 + 每人一层（WORK-015 / WORK-035 T-17）。
 *
 * <p><b>为什么是"拒绝"而不是"排队"</b>：请求已经在一次 HTTP 调用里等着了，排队只会让用户看到
 * 一个没有上限的转圈；而容器启动本身是秒级的。直接告诉他"现在忙"更诚实。
 *
 * <p><b>两层闸，缺一不可</b>：
 * <ul>
 *   <li>**全局**（默认 2）：容器是重资源，同时跑几个由宿主机决定；</li>
 *   <li>**每人 1**（WORK-035 新增）：没有这一层时，**一个账号就能把两个槽长期占死**——
 *       注册是全开放的，循环跑 SQL 就能把全站沙箱堵住，连带 Java/Python 一起。</li>
 * </ul>
 * 这是"SQL 日配额从 20 提到 500"能成立的前提：日上限变成熔断器而不是预算，
 * 真正的保护在这一层。
 *
 * <p>闸的粒度是**进程级**：单实例部署下它就是全局上限。将来多实例时每台各有 2 个，
 * 真正的全局上限要靠外部配额——那时再看要不要换。
 */
@Component
public class RunSlot {

    private final Semaphore slots;

    /**
     * 正在跑的用户。
     *
     * <p>用 `Set` 而不是 `Map<Long, Integer>`：每人上限是 1，计数没有意义。
     * `newKeySet()` 的 add 是原子的——"先 contains 再 put"分两步会在并发下同时通过。
     */
    private final Set<Long> activeUsers = ConcurrentHashMap.newKeySet();

    public RunSlot(SandboxProperties properties) {
        this.slots = new Semaphore(Math.max(1, properties.getMaxConcurrency()), true);
    }

    /** 不检查用户维度（既有调用点与测试用）。 */
    public Slot acquire() {
        return acquire(0L);
    }

    /**
     * 带用户维度地拿槽。
     *
     * <p>**先占用户标记、再抢全局槽**：反过来会出现"抢到全局槽但用户已有一个在跑"，
     * 那时要把刚抢到的槽还回去——多一条容易漏的清理路径。
     *
     * @param userId 0 表示不检查用户维度
     */
    public Slot acquire(long userId) {
        boolean userClaimed = userId != 0L && activeUsers.add(userId);
        if (userId != 0L && !userClaimed) {
            throw new BizException(ErrorCode.TOO_MANY_REQUESTS, "你还有一个查询在跑，等它结束再来");
        }

        boolean acquired;
        try {
            acquired = slots.tryAcquire(2, TimeUnit.SECONDS);
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            acquired = false;
        }
        if (!acquired) {
            // 全局没抢到：把用户标记还回去，否则这个人会被永久锁在"你还有一个在跑"
            if (userClaimed) {
                activeUsers.remove(userId);
            }
            throw new BizException(ErrorCode.TOO_MANY_REQUESTS, "现在运行的人有点多，稍后再试");
        }
        return new Slot(userId, userClaimed);
    }

    public final class Slot implements AutoCloseable {
        private final long userId;
        private final boolean userClaimed;
        private boolean released;

        private Slot(long userId, boolean userClaimed) {
            this.userId = userId;
            this.userClaimed = userClaimed;
        }

        @Override
        public void close() {
            if (released) {
                return;
            }
            released = true;
            // 先还全局槽、再移除用户标记：反过来的话，用户在"标记已移除、全局槽未释放"之间
            // 重试会拿到"忙"而实际上没人在跑——不致命，但会让人困惑。
            slots.release();
            if (userClaimed) {
                activeUsers.remove(userId);
            }
        }
    }
}
