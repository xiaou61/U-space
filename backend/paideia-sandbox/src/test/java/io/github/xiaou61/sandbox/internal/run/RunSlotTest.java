package io.github.xiaou61.sandbox.internal.run;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xiaou61.platform.BizException;
import org.junit.jupiter.api.Test;

/**
 * 并发闸（WORK-015 / WORK-035 T-17）。
 *
 * <p>**每人 1 这一层是"SQL 日配额提到 500"的前提**：注册全开放，没有这一层时
 * 一个账号循环跑 SQL 就能把两个全局槽长期占死，连带把 Java/Python 一起堵住。
 *
 * <p>这组用例要守的两件事：**同一人第二次被拒**，以及**被拒之后不会把人锁死**
 * （标记必须还回去，否则这个人之后永远收到"你还有一个在跑"）。
 */
class RunSlotTest {

    private static SandboxProperties props(int concurrency) {
        SandboxProperties properties = new SandboxProperties();
        properties.setMaxConcurrency(concurrency);
        return properties;
    }

    @Test
    void 同一人第二个请求被拒() {
        RunSlot slots = new RunSlot(props(4));
        try (RunSlot.Slot ignored = slots.acquire(7L)) {
            assertThatThrownBy(() -> slots.acquire(7L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("还有一个查询在跑");
        }
    }

    /** 不同用户互不影响（每人 1 是**每人**，不是全局 1）。 */
    @Test
    void 不同用户可以同时跑() {
        RunSlot slots = new RunSlot(props(4));
        try (RunSlot.Slot first = slots.acquire(7L);
             RunSlot.Slot second = slots.acquire(8L)) {
            assertThat(first).isNotNull();
            assertThat(second).isNotNull();
        }
    }

    /**
     * 释放之后能再跑。
     *
     * <p>这条守的是"标记有没有还回去"——漏还的话这个用户会被永久锁死，
     * 而现象是"他明明没在跑，却一直被告知还有一个在跑"。
     */
    @Test
    void 释放后同一人可以再次运行() {
        RunSlot slots = new RunSlot(props(4));
        try (RunSlot.Slot ignored = slots.acquire(7L)) {
            // 占着
        }
        try (RunSlot.Slot again = slots.acquire(7L)) {
            assertThat(again).isNotNull();
        }
    }

    /**
     * 全局槽拿不到时，**用户标记必须还回去**。
     *
     * <p>否则全局一忙，那个用户就被永久标记成"正在跑"——即使他一次都没跑成。
     */
    @Test
    void 全局满时用户标记被回滚() {
        RunSlot slots = new RunSlot(props(1));
        try (RunSlot.Slot occupied = slots.acquire(1L)) {
            assertThatThrownBy(() -> slots.acquire(2L))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining("有点多");
        }
        // 全局已释放：用户 2 应该能正常拿到槽（说明刚才那次失败没有留下标记）
        try (RunSlot.Slot retry = slots.acquire(2L)) {
            assertThat(retry).isNotNull();
        }
    }

    /** 不传 userId 的老调用点不检查用户维度：它们不共享"每人 1"。 */
    @Test
    void 不带用户的调用不做每人限制() {
        RunSlot slots = new RunSlot(props(4));
        try (RunSlot.Slot first = slots.acquire();
             RunSlot.Slot second = slots.acquire()) {
            assertThat(first).isNotNull();
            assertThat(second).isNotNull();
        }
    }

    /** close 幂等：重复 close 不该把槽还两次（那会让全局上限虚高）。 */
    @Test
    void 重复关闭不会多还槽() {
        RunSlot slots = new RunSlot(props(1));
        RunSlot.Slot slot = slots.acquire(7L);
        slot.close();
        slot.close();

        // 只有一个槽：如果还了两次，这里能同时拿到两个
        try (RunSlot.Slot first = slots.acquire(1L)) {
            assertThatThrownBy(() -> slots.acquire(2L)).isInstanceOf(BizException.class);
        }
    }
}
