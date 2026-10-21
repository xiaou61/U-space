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

    /**
     * 完整载荷（请求体 + 响应体）的开关（WORK-030 阶段 B）。
     *
     * <p>默认**开**：这一项存在的目的是排查"模型为什么这样答"，关掉就看不到。
     * 关掉时不写库，管理端显示「已停用」而不是空白（REQ-064）。
     */
    private boolean payloadEnabled = true;

    /**
     * 载荷的保留天数（U-20：30 天）。**与 {@link #retentionDays} 独立**：
     * spans 是账目级（90 天），payload 是诊断级（30 天）且体积大得多。
     *
     * <p>量级估算：一天 100 次调用 × 每次 5 轮 × 平均 20KB ≈ 10MB/天，30 天约 300MB。
     */
    private int payloadRetentionDays = 30;

    /**
     * 单条载荷的体积上限（字节）。超限时**存占位记录而不是丢弃**（REQ-066）——
     * 静默丢会让"这一轮为什么没有请求体"变成一个新的谜。
     */
    private int payloadMaxBytes = 1_048_576;

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
    public boolean isPayloadEnabled() { return payloadEnabled; }
    public void setPayloadEnabled(boolean payloadEnabled) { this.payloadEnabled = payloadEnabled; }
    public int getPayloadRetentionDays() { return payloadRetentionDays; }
    public void setPayloadRetentionDays(int payloadRetentionDays) { this.payloadRetentionDays = payloadRetentionDays; }
    public int getPayloadMaxBytes() { return payloadMaxBytes; }
    public void setPayloadMaxBytes(int payloadMaxBytes) { this.payloadMaxBytes = payloadMaxBytes; }
}
