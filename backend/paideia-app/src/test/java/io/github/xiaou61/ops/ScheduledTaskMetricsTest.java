package io.github.xiaou61.ops;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 定时任务指标的三种情形（WORK-028 REQ-016/REQ-018）。
 *
 * <p>关键断言是**「从未运行」不能被当成「失败」**：两者都表现为"没有新内容"，
 * 但一个是没开启、一个是坏了。区分靠 `last_run_epoch_seconds` 是否为 0。
 */
class ScheduledTaskMetricsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

    @Test
    @DisplayName("已知任务在从未运行时就已注册，且 last_run 为 0")
    void knownTasksArePreRegistered() {
        new ScheduledTaskMetrics(registry);

        for (String task : ScheduledTaskMetrics.KNOWN_TASKS) {
            assertThat(registry.get("paideia.scheduled.last.run.epoch.seconds").tag("task", task).gauge().value())
                    .as("任务 %s 的 last_run 应为 0", task)
                    .isZero();
            // 初始 success 为 1：从未运行不等于失败，判据是 last_run
            assertThat(registry.get("paideia.scheduled.last.success").tag("task", task).gauge().value())
                    .isEqualTo(1.0);
        }
    }

    @Test
    @DisplayName("执行成功后 last_run 有值、success 为 1、耗时按秒记")
    void successIsRecorded() {
        ScheduledTaskMetrics metrics = new ScheduledTaskMetrics(registry);
        metrics.record("expired-state-purger", Duration.ofMillis(1500), null);

        assertThat(registry.get("paideia.scheduled.last.success").tag("task", "expired-state-purger")
                .gauge().value()).isEqualTo(1.0);
        assertThat(registry.get("paideia.scheduled.last.run.epoch.seconds").tag("task", "expired-state-purger")
                .gauge().value()).isPositive();
        assertThat(registry.get("paideia.scheduled.last.duration.seconds").tag("task", "expired-state-purger")
                .gauge().value()).isEqualTo(1.5);
        assertThat(registry.get("paideia.scheduled.runs.total").tag("task", "expired-state-purger")
                .gauge().value()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("失败记 0，但仍然有 last_run（能区分「失败」与「从未运行」）")
    void failureIsRecorded() {
        ScheduledTaskMetrics metrics = new ScheduledTaskMetrics(registry);
        metrics.record("telemetry-retention", Duration.ofMillis(20), new IllegalStateException("boom"));

        assertThat(registry.get("paideia.scheduled.last.success").tag("task", "telemetry-retention")
                .gauge().value()).isZero();
        assertThat(registry.get("paideia.scheduled.last.run.epoch.seconds").tag("task", "telemetry-retention")
                .gauge().value()).isPositive();
    }

    @Test
    @DisplayName("未知任务名的记录被忽略，不会凭空长出新指标")
    void unknownTaskIsIgnored() {
        ScheduledTaskMetrics metrics = new ScheduledTaskMetrics(registry);
        metrics.record("no-such-task", Duration.ofSeconds(1), null);

        assertThat(registry.find("paideia.scheduled.runs.total").tag("task", "no-such-task").gauge()).isNull();
    }
}
