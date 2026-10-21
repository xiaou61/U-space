package io.github.xiaou61.ops;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 运维面板的阈值与连接参数（WORK-028）。
 *
 * <p>阈值放配置而不是写进面板 JSON：写在 Grafana 里就变成了"只有面板知道"，前端拿不到、
 * 测试测不到，改一次要动 JSON。这里一处定义，摘要接口与面板都以它为准。
 */
@ConfigurationProperties(prefix = "paideia.ops")
public class OpsProperties {

    /** Prometheus 的查询地址。只应指向回环。 */
    private String prometheusUrl = "http://127.0.0.1:9090";

    /** 单次查询超时；一个慢查询不得拖住整页。 */
    private long queryTimeoutMs = 2000;

    /** Grafana 地址。反向代理唯一的目标。 */
    private String grafanaUrl = "http://127.0.0.1:3000";

    private final Thresholds thresholds = new Thresholds();

    public String getPrometheusUrl() {
        return prometheusUrl;
    }

    public void setPrometheusUrl(String prometheusUrl) {
        this.prometheusUrl = prometheusUrl;
    }

    public long getQueryTimeoutMs() {
        return queryTimeoutMs;
    }

    public void setQueryTimeoutMs(long queryTimeoutMs) {
        this.queryTimeoutMs = queryTimeoutMs;
    }

    public String getGrafanaUrl() {
        return grafanaUrl;
    }

    public void setGrafanaUrl(String grafanaUrl) {
        this.grafanaUrl = grafanaUrl;
    }

    public Thresholds getThresholds() {
        return thresholds;
    }

    public static class Thresholds {

        /** 磁盘已用比例达到此值报警告。 */
        private double diskUsedWarn = 0.70;

        /** 磁盘已用比例达到此值报异常。 */
        private double diskUsedError = 0.85;

        /** systemd 单元内存占上限的比例达到此值报警告（Laya 常态就在这一档）。 */
        private double unitMemoryWarn = 0.75;

        /** 单元内存占上限的比例达到此值报异常。 */
        private double unitMemoryError = 0.90;

        /** 备份超过这么多小时没成功就算陈旧。 */
        private long backupStaleHours = 36;

        public double getDiskUsedWarn() {
            return diskUsedWarn;
        }

        public void setDiskUsedWarn(double diskUsedWarn) {
            this.diskUsedWarn = diskUsedWarn;
        }

        public double getDiskUsedError() {
            return diskUsedError;
        }

        public void setDiskUsedError(double diskUsedError) {
            this.diskUsedError = diskUsedError;
        }

        public double getUnitMemoryWarn() {
            return unitMemoryWarn;
        }

        public void setUnitMemoryWarn(double unitMemoryWarn) {
            this.unitMemoryWarn = unitMemoryWarn;
        }

        public double getUnitMemoryError() {
            return unitMemoryError;
        }

        public void setUnitMemoryError(double unitMemoryError) {
            this.unitMemoryError = unitMemoryError;
        }

        public long getBackupStaleHours() {
            return backupStaleHours;
        }

        public void setBackupStaleHours(long backupStaleHours) {
            this.backupStaleHours = backupStaleHours;
        }
    }
}
