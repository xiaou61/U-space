package com.xiaou.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * 并发工具类
 * <p>
 * 提供并发控制原语、限流器、熔断器、分布式锁抽象等功能
 * </p>
 * <p>
 * 核心特性：
 * <ul>
 *     <li>限流器：令牌桶、滑动窗口</li>
 *     <li>熔断器：自动熔断与恢复</li>
 *     <li>锁工具：安全锁操作、读写锁、分段锁</li>
 *     <li>并发计数器：高性能计数与统计</li>
 *     <li>同步屏障：CountDownLatch、CyclicBarrier封装</li>
 *     <li>缓存工具：线程安全的懒加载缓存</li>
 * </ul>
 * </p>
 *
 * @author xiaou
 */
@Slf4j
public class ConcurrentUtils {

    /**
     * 限流器缓存
     */
    private static final Map<String, RateLimiter> RATE_LIMITER_CACHE = new ConcurrentHashMap<>();

    /**
     * 熔断器缓存
     */
    private static final Map<String, CircuitBreaker> CIRCUIT_BREAKER_CACHE = new ConcurrentHashMap<>();

    /**
     * 分段锁缓存
     */
    private static final Map<String, StripedLock> STRIPED_LOCK_CACHE = new ConcurrentHashMap<>();

    // ==================== 限流器 ====================

    /**
     * 获取或创建限流器
     *
     * @param name           限流器名称
     * @param permitsPerSecond 每秒许可数
     * @return RateLimiter
     */
    public static RateLimiter getRateLimiter(String name, double permitsPerSecond) {
        return RATE_LIMITER_CACHE.computeIfAbsent(name, k -> new RateLimiter(permitsPerSecond));
    }

    /**
     * 移除限流器
     */
    public static void removeRateLimiter(String name) {
        RATE_LIMITER_CACHE.remove(name);
    }

    /**
     * 令牌桶限流器
     */
    public static class RateLimiter {
        private final double permitsPerSecond;
        private final long intervalNanos;
        private final AtomicLong nextFreeTicketNanos;
        private final int maxPermits;
        private double storedPermits;
        private final Object mutex = new Object();

        public RateLimiter(double permitsPerSecond) {
            this(permitsPerSecond, (int) permitsPerSecond);
        }

        public RateLimiter(double permitsPerSecond, int maxBurstPermits) {
            this.permitsPerSecond = permitsPerSecond;
            this.intervalNanos = (long) (1_000_000_000.0 / permitsPerSecond);
            this.nextFreeTicketNanos = new AtomicLong(System.nanoTime());
            this.maxPermits = maxBurstPermits;
            this.storedPermits = maxBurstPermits;
        }

        /**
         * 尝试获取许可（非阻塞）
         */
        public boolean tryAcquire() {
            return tryAcquire(1);
        }

        /**
         * 尝试获取指定数量的许可（非阻塞）
         */
        public boolean tryAcquire(int permits) {
            synchronized (mutex) {
                long now = System.nanoTime();
                reSync(now);
                if (storedPermits >= permits) {
                    storedPermits -= permits;
                    return true;
                }
                return false;
            }
        }

        /**
         * 尝试在指定时间内获取许可
         */
        public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
            long timeoutNanos = unit.toNanos(timeout);
            long deadline = System.nanoTime() + timeoutNanos;

            while (System.nanoTime() < deadline) {
                if (tryAcquire(permits)) {
                    return true;
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return false;
        }

        /**
         * 阻塞获取许可
         */
        public void acquire() {
            acquire(1);
        }

        /**
         * 阻塞获取指定数量的许可
         */
        public void acquire(int permits) {
            while (!tryAcquire(permits)) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * 重新同步令牌
         */
        private void reSync(long now) {
            long lastTicket = nextFreeTicketNanos.get();
            if (now > lastTicket) {
                double newPermits = (now - lastTicket) / (double) intervalNanos;
                storedPermits = Math.min(maxPermits, storedPermits + newPermits);
                nextFreeTicketNanos.set(now);
            }
        }

        /**
         * 获取当前QPS配置
         */
        public double getPermitsPerSecond() {
            return permitsPerSecond;
        }

        /**
         * 带限流执行任务
         */
        public <T> T executeWithLimit(Supplier<T> supplier) {
            acquire();
            return supplier.get();
        }

        /**
         * 带限流执行任务（非阻塞，失败返回null）
         */
        public <T> T tryExecuteWithLimit(Supplier<T> supplier) {
            if (tryAcquire()) {
                return supplier.get();
            }
            return null;
        }
    }

    /**
     * 滑动窗口限流器
     */
    public static class SlidingWindowRateLimiter {
        private final int windowSize;
        private final long windowDurationMs;
        private final AtomicLong[] windows;
        private final AtomicInteger currentIndex;
        private final AtomicLong lastSlideTime;
        private final int maxRequests;

        /**
         * @param maxRequests      窗口内最大请求数
         * @param windowDurationMs 窗口时长（毫秒）
         * @param windowSize       滑动窗口分片数
         */
        public SlidingWindowRateLimiter(int maxRequests, long windowDurationMs, int windowSize) {
            this.maxRequests = maxRequests;
            this.windowDurationMs = windowDurationMs;
            this.windowSize = windowSize;
            this.windows = new AtomicLong[windowSize];
            for (int i = 0; i < windowSize; i++) {
                windows[i] = new AtomicLong(0);
            }
            this.currentIndex = new AtomicInteger(0);
            this.lastSlideTime = new AtomicLong(System.currentTimeMillis());
        }

        /**
         * 尝试获取许可
         */
        public boolean tryAcquire() {
            slideWindow();
            long total = 0;
            for (AtomicLong window : windows) {
                total += window.get();
            }
            if (total < maxRequests) {
                windows[currentIndex.get()].incrementAndGet();
                return true;
            }
            return false;
        }

        /**
         * 滑动窗口
         */
        private void slideWindow() {
            long now = System.currentTimeMillis();
            long last = lastSlideTime.get();
            long slotDuration = windowDurationMs / windowSize;
            long elapsed = now - last;

            if (elapsed >= slotDuration) {
                int slotsToSlide = (int) Math.min(elapsed / slotDuration, windowSize);
                if (lastSlideTime.compareAndSet(last, now)) {
                    for (int i = 0; i < slotsToSlide; i++) {
                        int nextIndex = (currentIndex.get() + 1) % windowSize;
                        windows[nextIndex].set(0);
                        currentIndex.set(nextIndex);
                    }
                }
            }
        }

        /**
         * 获取当前窗口内的请求数
         */
        public long getCurrentCount() {
            slideWindow();
            long total = 0;
            for (AtomicLong window : windows) {
                total += window.get();
            }
            return total;
        }
    }

    // ==================== 熔断器 ====================

    /**
     * 获取或创建熔断器
     *
     * @param name              熔断器名称
     * @param failureThreshold  失败阈值
     * @param resetTimeoutMs    重置超时（毫秒）
     * @return CircuitBreaker
     */
    public static CircuitBreaker getCircuitBreaker(String name, int failureThreshold, long resetTimeoutMs) {
        return CIRCUIT_BREAKER_CACHE.computeIfAbsent(name,
                k -> new CircuitBreaker(failureThreshold, resetTimeoutMs));
    }

    /**
     * 移除熔断器
     */
    public static void removeCircuitBreaker(String name) {
        CIRCUIT_BREAKER_CACHE.remove(name);
    }

    /**
     * 熔断器
     */
    public static class CircuitBreaker {
        private final int failureThreshold;
        private final long resetTimeoutMs;
        private final AtomicInteger failureCount;
        private final AtomicLong lastFailureTime;
        private volatile State state;

        public enum State {
            CLOSED,      // 正常状态
            OPEN,        // 熔断状态
            HALF_OPEN    // 半开状态
        }

        public CircuitBreaker(int failureThreshold, long resetTimeoutMs) {
            this.failureThreshold = failureThreshold;
            this.resetTimeoutMs = resetTimeoutMs;
            this.failureCount = new AtomicInteger(0);
            this.lastFailureTime = new AtomicLong(0);
            this.state = State.CLOSED;
        }

        /**
         * 执行带熔断保护的任务
         */
        public <T> T execute(Supplier<T> supplier) {
            if (!allowRequest()) {
                throw new CircuitBreakerOpenException("熔断器已打开，拒绝请求");
            }

            try {
                T result = supplier.get();
                onSuccess();
                return result;
            } catch (Exception e) {
                onFailure();
                throw e;
            }
        }

        /**
         * 执行带熔断保护的任务（带降级）
         */
        public <T> T executeWithFallback(Supplier<T> supplier, Supplier<T> fallback) {
            if (!allowRequest()) {
                log.warn("熔断器已打开，执行降级逻辑");
                return fallback.get();
            }

            try {
                T result = supplier.get();
                onSuccess();
                return result;
            } catch (Exception e) {
                onFailure();
                log.warn("任务执行失败，执行降级逻辑: {}", e.getMessage());
                return fallback.get();
            }
        }

        /**
         * 是否允许请求
         */
        public boolean allowRequest() {
            if (state == State.CLOSED) {
                return true;
            }

            if (state == State.OPEN) {
                long now = System.currentTimeMillis();
                if (now - lastFailureTime.get() > resetTimeoutMs) {
                    state = State.HALF_OPEN;
                    log.info("熔断器进入半开状态");
                    return true;
                }
                return false;
            }

            // HALF_OPEN状态，允许一个请求通过
            return true;
        }

        /**
         * 成功回调
         */
        public void onSuccess() {
            if (state == State.HALF_OPEN) {
                state = State.CLOSED;
                failureCount.set(0);
                log.info("熔断器已关闭");
            }
        }

        /**
         * 失败回调
         */
        public void onFailure() {
            lastFailureTime.set(System.currentTimeMillis());
            int failures = failureCount.incrementAndGet();

            if (state == State.HALF_OPEN) {
                state = State.OPEN;
                log.warn("熔断器从半开状态重新打开");
            } else if (failures >= failureThreshold) {
                state = State.OPEN;
                log.warn("失败次数达到阈值{}，熔断器已打开", failureThreshold);
            }
        }

        /**
         * 获取当前状态
         */
        public State getState() {
            // 检查是否需要转换状态
            allowRequest();
            return state;
        }

        /**
         * 强制重置熔断器
         */
        public void reset() {
            state = State.CLOSED;
            failureCount.set(0);
            lastFailureTime.set(0);
            log.info("熔断器已重置");
        }

        /**
         * 获取失败次数
         */
        public int getFailureCount() {
            return failureCount.get();
        }
    }

    /**
     * 熔断器打开异常
     */
    public static class CircuitBreakerOpenException extends RuntimeException {
        public CircuitBreakerOpenException(String message) {
            super(message);
        }
    }

    // ==================== 锁工具 ====================

    /**
     * 安全执行锁内操作
     *
     * @param lock     锁
     * @param supplier 任务
     * @return 执行结果
     */
    public static <T> T withLock(Lock lock, Supplier<T> supplier) {
        lock.lock();
        try {
            return supplier.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 安全执行锁内操作（无返回值）
     */
    public static void withLock(Lock lock, Runnable task) {
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 尝试获取锁并执行
     *
     * @param lock     锁
     * @param timeout  超时时间
     * @param unit     时间单位
     * @param supplier 任务
     * @return 执行结果，获取锁失败返回null
     */
    public static <T> T tryWithLock(Lock lock, long timeout, TimeUnit unit, Supplier<T> supplier) {
        try {
            if (lock.tryLock(timeout, unit)) {
                try {
                    return supplier.get();
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    /**
     * 安全执行读锁内操作
     */
    public static <T> T withReadLock(ReadWriteLock rwLock, Supplier<T> supplier) {
        Lock readLock = rwLock.readLock();
        readLock.lock();
        try {
            return supplier.get();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * 安全执行写锁内操作
     */
    public static <T> T withWriteLock(ReadWriteLock rwLock, Supplier<T> supplier) {
        Lock writeLock = rwLock.writeLock();
        writeLock.lock();
        try {
            return supplier.get();
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * 获取或创建分段锁
     */
    public static StripedLock getStripedLock(String name, int stripes) {
        return STRIPED_LOCK_CACHE.computeIfAbsent(name, k -> new StripedLock(stripes));
    }

    /**
     * 分段锁（减少锁竞争）
     */
    public static class StripedLock {
        private final Lock[] locks;
        private final int mask;

        public StripedLock(int stripes) {
            // 确保stripes是2的幂
            int size = 1;
            while (size < stripes) {
                size <<= 1;
            }
            this.mask = size - 1;
            this.locks = new Lock[size];
            for (int i = 0; i < size; i++) {
                locks[i] = new ReentrantLock();
            }
        }

        /**
         * 根据key获取锁
         */
        public Lock getLock(Object key) {
            return locks[hash(key) & mask];
        }

        /**
         * 使用指定key的锁执行任务
         */
        public <T> T withLock(Object key, Supplier<T> supplier) {
            Lock lock = getLock(key);
            lock.lock();
            try {
                return supplier.get();
            } finally {
                lock.unlock();
            }
        }

        private int hash(Object key) {
            int h = key.hashCode();
            return h ^ (h >>> 16);
        }
    }

    // ==================== 并发计数器 ====================

    /**
     * 高性能计数器
     */
    public static class Counter {
        private final LongAdder adder = new LongAdder();

        public void increment() {
            adder.increment();
        }

        public void add(long delta) {
            adder.add(delta);
        }

        public void decrement() {
            adder.decrement();
        }

        public long get() {
            return adder.sum();
        }

        public void reset() {
            adder.reset();
        }

        public long getAndReset() {
            return adder.sumThenReset();
        }
    }

    /**
     * 并发统计器
     */
    public static class Statistics {
        private final LongAdder count = new LongAdder();
        private final LongAdder sum = new LongAdder();
        private final AtomicLong min = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong max = new AtomicLong(Long.MIN_VALUE);

        public void record(long value) {
            count.increment();
            sum.add(value);
            updateMin(value);
            updateMax(value);
        }

        private void updateMin(long value) {
            long current;
            do {
                current = min.get();
                if (value >= current) {
                    return;
                }
            } while (!min.compareAndSet(current, value));
        }

        private void updateMax(long value) {
            long current;
            do {
                current = max.get();
                if (value <= current) {
                    return;
                }
            } while (!max.compareAndSet(current, value));
        }

        public long getCount() {
            return count.sum();
        }

        public long getSum() {
            return sum.sum();
        }

        public long getMin() {
            long val = min.get();
            return val == Long.MAX_VALUE ? 0 : val;
        }

        public long getMax() {
            long val = max.get();
            return val == Long.MIN_VALUE ? 0 : val;
        }

        public double getAverage() {
            long c = count.sum();
            return c == 0 ? 0 : (double) sum.sum() / c;
        }

        public void reset() {
            count.reset();
            sum.reset();
            min.set(Long.MAX_VALUE);
            max.set(Long.MIN_VALUE);
        }

        @Override
        public String toString() {
            return String.format("Statistics{count=%d, sum=%d, min=%d, max=%d, avg=%.2f}",
                    getCount(), getSum(), getMin(), getMax(), getAverage());
        }
    }

    // ==================== 同步屏障 ====================

    /**
     * 创建倒计时器
     *
     * @param count 计数
     * @return CountDownLatch
     */
    public static CountDownLatch createLatch(int count) {
        return new CountDownLatch(count);
    }

    /**
     * 等待倒计时完成（带超时）
     */
    public static boolean awaitLatch(CountDownLatch latch, long timeout, TimeUnit unit) {
        try {
            return latch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 等待倒计时完成
     */
    public static void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("等待被中断", e);
        }
    }

    /**
     * 创建循环屏障
     *
     * @param parties 参与方数量
     * @return CyclicBarrier
     */
    public static CyclicBarrier createBarrier(int parties) {
        return new CyclicBarrier(parties);
    }

    /**
     * 创建循环屏障（带回调）
     */
    public static CyclicBarrier createBarrier(int parties, Runnable barrierAction) {
        return new CyclicBarrier(parties, barrierAction);
    }

    /**
     * 等待屏障
     */
    public static int awaitBarrier(CyclicBarrier barrier) {
        try {
            return barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("等待屏障失败", e);
        }
    }

    /**
     * 等待屏障（带超时）
     */
    public static int awaitBarrier(CyclicBarrier barrier, long timeout, TimeUnit unit) {
        try {
            return barrier.await(timeout, unit);
        } catch (InterruptedException | BrokenBarrierException | TimeoutException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("等待屏障失败", e);
        }
    }

    // ==================== 信号量工具 ====================

    /**
     * 创建信号量
     */
    public static Semaphore createSemaphore(int permits) {
        return new Semaphore(permits);
    }

    /**
     * 创建公平信号量
     */
    public static Semaphore createFairSemaphore(int permits) {
        return new Semaphore(permits, true);
    }

    /**
     * 使用信号量执行任务
     */
    public static <T> T withSemaphore(Semaphore semaphore, Supplier<T> supplier) {
        try {
            semaphore.acquire();
            try {
                return supplier.get();
            } finally {
                semaphore.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取信号量被中断", e);
        }
    }

    /**
     * 尝试使用信号量执行任务
     */
    public static <T> T tryWithSemaphore(Semaphore semaphore, long timeout, TimeUnit unit,
                                          Supplier<T> supplier, T defaultValue) {
        try {
            if (semaphore.tryAcquire(timeout, unit)) {
                try {
                    return supplier.get();
                } finally {
                    semaphore.release();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return defaultValue;
    }

    // ==================== 延迟初始化 ====================

    /**
     * 线程安全的懒加载容器
     */
    public static class Lazy<T> {
        private volatile T value;
        private final Supplier<T> supplier;
        private final Object lock = new Object();

        public Lazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public T get() {
            T result = value;
            if (result == null) {
                synchronized (lock) {
                    result = value;
                    if (result == null) {
                        value = result = supplier.get();
                    }
                }
            }
            return result;
        }

        public boolean isInitialized() {
            return value != null;
        }

        public void reset() {
            synchronized (lock) {
                value = null;
            }
        }
    }

    /**
     * 创建懒加载容器
     */
    public static <T> Lazy<T> lazy(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * 带过期时间的缓存值
     */
    public static class ExpiringValue<T> {
        private volatile T value;
        private volatile long expireTime;
        private final Supplier<T> supplier;
        private final long ttlMs;
        private final Object lock = new Object();

        public ExpiringValue(Supplier<T> supplier, long ttlMs) {
            this.supplier = supplier;
            this.ttlMs = ttlMs;
        }

        public T get() {
            long now = System.currentTimeMillis();
            if (value == null || now >= expireTime) {
                synchronized (lock) {
                    now = System.currentTimeMillis();
                    if (value == null || now >= expireTime) {
                        value = supplier.get();
                        expireTime = now + ttlMs;
                    }
                }
            }
            return value;
        }

        public void invalidate() {
            synchronized (lock) {
                value = null;
                expireTime = 0;
            }
        }

        public boolean isExpired() {
            return System.currentTimeMillis() >= expireTime;
        }
    }

    /**
     * 创建带过期时间的缓存值
     */
    public static <T> ExpiringValue<T> expiringValue(Supplier<T> supplier, Duration ttl) {
        return new ExpiringValue<>(supplier, ttl.toMillis());
    }

    // ==================== 单次执行 ====================

    /**
     * 只执行一次的任务容器
     */
    public static class Once {
        private final AtomicInteger executed = new AtomicInteger(0);
        private final CountDownLatch latch = new CountDownLatch(1);

        /**
         * 执行任务（只有第一次调用会执行）
         */
        public void run(Runnable task) {
            if (executed.compareAndSet(0, 1)) {
                try {
                    task.run();
                } finally {
                    latch.countDown();
                }
            } else {
                // 等待首次执行完成
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        /**
         * 是否已执行
         */
        public boolean isExecuted() {
            return executed.get() > 0;
        }
    }

    /**
     * 创建单次执行容器
     */
    public static Once once() {
        return new Once();
    }

    // ==================== 并发映射工具 ====================

    /**
     * 原子更新Map中的值
     */
    public static <K, V> V computeIfAbsent(ConcurrentMap<K, V> map, K key, Supplier<V> supplier) {
        V value = map.get(key);
        if (value == null) {
            value = supplier.get();
            V existing = map.putIfAbsent(key, value);
            if (existing != null) {
                value = existing;
            }
        }
        return value;
    }

    /**
     * 创建有界阻塞队列
     */
    public static <T> BlockingQueue<T> createBoundedQueue(int capacity) {
        return new LinkedBlockingQueue<>(capacity);
    }

    /**
     * 创建优先级阻塞队列
     */
    public static <T> BlockingQueue<T> createPriorityQueue() {
        return new PriorityBlockingQueue<>();
    }

    /**
     * 创建延迟队列
     */
    public static <T extends Delayed> DelayQueue<T> createDelayQueue() {
        return new DelayQueue<>();
    }

    // ==================== 工具方法 ====================

    /**
     * 安全休眠
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 安全休眠
     */
    public static void sleep(Duration duration) {
        sleep(duration.toMillis());
    }

    /**
     * 自旋等待条件满足
     */
    public static boolean spinWait(Supplier<Boolean> condition, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.get()) {
                return true;
            }
            Thread.onSpinWait();
        }
        return false;
    }

    /**
     * 获取当前线程名
     */
    public static String currentThreadName() {
        return Thread.currentThread().getName();
    }

    /**
     * 设置当前线程名
     */
    public static void setThreadName(String name) {
        Thread.currentThread().setName(name);
    }

    /**
     * 私有构造函数
     */
    private ConcurrentUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }
}
