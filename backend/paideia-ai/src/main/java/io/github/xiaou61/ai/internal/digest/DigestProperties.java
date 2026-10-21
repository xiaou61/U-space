package io.github.xiaou61.ai.internal.digest;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 每日技术速递的配置（WORK-027）。
 *
 * <p>**默认关闭**，与 essay / decision 同一条纪律：它要访问多个外部站点、每天花一次 AI 调用，
 * 打开是部署时的显式决定。
 *
 * <p><b>源清单在配置里而不是代码里</b>：加一个 RSS 源不该改代码。这是与 WORK-025 不同的一处
 * ——牛客是一个特殊 API，速递的 RSS 源是同质的。
 */
@ConfigurationProperties(prefix = "paideia.ai.digest")
public class DigestProperties {

    /** 是否启用每日抓取。关闭时不注册定时任务。 */
    private boolean enabled = false;

    /**
     * GitHub token。**可选增强**：配置后 GitHub 主通路走 Search API（5000/时、稳定）；
     * 不配则走页面解析、失败再退回匿名 Search API（60/时，一天一次够用）。
     * **值只进被忽略的本地配置文件**，仓库里只留环境变量占位。
     */
    private String githubToken = "";

    /** 单次运行最多入库条数。**这是成本闸**：决定喂给 AI 的候选池上限。 */
    private int maxItemsPerRun = 60;

    /** 每周最多取多少条日报素材做周报。超了按天分段摘要。 */
    private int weeklyMaxItems = 150;

    /** 抓多久以内的内容（小时）。30 覆盖「昨天至今」并留容错。 */
    private int lookbackHours = 30;

    /** 单次 HTTP 抓取超时（秒）。 */
    private int timeoutSeconds = 20;

    /** AI 调用超时（秒）。一次调用要处理几十条，给足。 */
    private int aiTimeoutSeconds = 180;

    /** 抓来的内容直出（不设草稿）。用户裁决 D-07。 */
    private boolean publishDirectly = true;

    /** RSS/Atom 源清单。 */
    private List<RssSource> rss = new ArrayList<>();

    /** 一个 RSS 源。 */
    public static class RssSource {

        /** 源标识（小写），如 qbitai。 */
        private String key = "";

        /** 显示名，如「量子位」。 */
        private String name = "";

        /** feed 地址。 */
        private String url = "";

        /** 前端分组：news | github | community | hackernews。 */
        private String group = "news";

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }

    public int getMaxItemsPerRun() {
        return maxItemsPerRun;
    }

    public void setMaxItemsPerRun(int maxItemsPerRun) {
        this.maxItemsPerRun = maxItemsPerRun;
    }

    public int getWeeklyMaxItems() {
        return weeklyMaxItems;
    }

    public void setWeeklyMaxItems(int weeklyMaxItems) {
        this.weeklyMaxItems = weeklyMaxItems;
    }

    public int getLookbackHours() {
        return lookbackHours;
    }

    public void setLookbackHours(int lookbackHours) {
        this.lookbackHours = lookbackHours;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getAiTimeoutSeconds() {
        return aiTimeoutSeconds;
    }

    public void setAiTimeoutSeconds(int aiTimeoutSeconds) {
        this.aiTimeoutSeconds = aiTimeoutSeconds;
    }

    public boolean isPublishDirectly() {
        return publishDirectly;
    }

    public void setPublishDirectly(boolean publishDirectly) {
        this.publishDirectly = publishDirectly;
    }

    public List<RssSource> getRss() {
        return rss;
    }

    public void setRss(List<RssSource> rss) {
        this.rss = rss;
    }
}
