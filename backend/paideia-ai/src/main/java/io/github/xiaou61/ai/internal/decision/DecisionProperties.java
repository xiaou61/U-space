package io.github.xiaou61.ai.internal.decision;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 决策模型（Laya）的配置（WORK-026）。
 *
 * <p>**默认关闭**，与沙箱同一条纪律：它要连一个外部 Python 进程，那个进程不在就调用必然超时，
 * 而这会拖慢每条走过滤的内容。打开是部署时的显式决定。
 *
 * <p>{@code minConfidence} 不是"准确率下限"，是"低于这个数就别用模型答案、退回规则"的开关。
 * 实测中文概率偏乐观（官方自测 zh-CN 的 ECE 0.219），0.7 只表达"模型自己也不确定"，不代表七成命中。
 */
@ConfigurationProperties(prefix = "paideia.ai.decision")
public class DecisionProperties {

    /** 是否启用决策模型。关闭时分类器返回 empty，调用方走规则降级。 */
    private boolean enabled = false;

    /** laya-serve 的地址。**只在回环或内网使用**：该服务默认无鉴权（除 api-key 外）。 */
    private String baseUrl = "http://127.0.0.1:8091";

    /** 与 laya-serve 的 `LAYA_API_KEY` 一致；留空表示对方没开鉴权。 */
    private String apiKey = "";

    /**
     * 单次判定超时（毫秒）。
     *
     * <p>实测（4 核、`LAYA_THREADS=2`）：单条 p50 147~260ms；并发 8 时 p50 1219ms / p95 1633ms；
     * 并发 16 时 p50 1853ms / p95 2965ms。给 5000 是为了在并发 16 时仍有余量——
     * 3 秒会刚好卡在 p95 上，偶发超时会白白降级到规则。
     *
     * <p>首次加载模型要 16~22 秒，那时的请求仍会超时降级——那是对的语义：
     * 不该为了等冷启动把每条内容都卡住。
     */
    private int timeoutMs = 5000;

    /**
     * 低于此概率就当作"模型没把握"，返回 empty 交给调用方降级。
     *
     * <p>0.7 配的是 `choice` 三分类的 softmax 概率（见 {@link LayaContentClassifier} 里
     * 为什么不用 noul）：实测明确的技术面经能到 0.98，非技术帖落在 career 一侧。
     * **别把它理解成准确率**——中文概率没做过温度拟合，官方自测 zh-CN 的 ECE 是 0.219，
     * 这个数只用来区分"模型很确定"和"模型在猜"。
     */
    private double minConfidence = 0.7;

    /** 送进模型的文本长度上限（字符）。超过就截断，避免长帖把推理拖成秒级。 */
    private int maxTextChars = 4000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public double getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }

    public int getMaxTextChars() {
        return maxTextChars;
    }

    public void setMaxTextChars(int maxTextChars) {
        this.maxTextChars = maxTextChars;
    }
}
