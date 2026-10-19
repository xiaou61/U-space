package io.github.xiaou61.ai.internal.telemetry;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 采集配置（WORK-022）。
 *
 * <p>六项都有安全的默认值：**默认全量采集、不采样、留 90 天**。
 * 采样为 1.0 是刻意的——先拿到完整数据，等量真的上来了再开采样，
 * 而不是一开始就看不到失败的细节。
 */
@ConfigurationProperties(prefix = "paideia.ai.telemetry")
public class TelemetryProperties {

    /** 总开关。关掉后不产生任何记录，业务行为完全不变（也不删历史数据）。 */
    private boolean enabled = true;

    /** 成功轮次的保留比例（0–1）。失败 / 超时 / 中断**永远保留**。 */
    private double sampling = 1.0;

    /** 保留天数。清理任务按它删；统计接口的时间窗也按它收敛。 */
    private int retentionDays = 90;

    /** 队列容量。满则丢并计数，绝不阻塞调用方。 */
    private int queueCapacity = 10_000;

    /** 停在 running 超过这么多小时的，由清理任务标成 interrupted。 */
    private int stuckAfterHours = 6;

    /** 单次批处理的条数上限。 */
    private int batchSize = 50;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public double getSampling() { return sampling; }
    public void setSampling(double sampling) { this.sampling = sampling; }
    public int getRetentionDays() { return retentionDays; }
    public void setRetentionDays(int retentionDays) { this.retentionDays = retentionDays; }
    public int getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
    public int getStuckAfterHours() { return stuckAfterHours; }
    public void setStuckAfterHours(int stuckAfterHours) { this.stuckAfterHours = stuckAfterHours; }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
}
