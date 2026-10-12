package io.github.xiaou61.ai.internal.credential;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 配置。
 *
 * <p><b>{@code master-key} 是必填的</b>（base64 的 32 字节）：它保护用户存进来的模型 Key，
 * 没有它就只能明文存，而"静默降级成明文"比启动失败危险得多——启动失败会立刻被发现，
 * 明文落库不会。校验在 {@link CredentialCipher} 的构造期做，缺它整个应用起不来。
 *
 * <p>环境变量 `PAIDEIA_AI_MASTER_KEY` 对应 `paideia.ai.master-key`（Spring 的宽松绑定）。
 * 值不进仓库：本地放被忽略的配置文件，服务器放 `application.yml`。
 */
@ConfigurationProperties(prefix = "paideia.ai")
public class AiProperties {

    /** base64 编码的 32 字节主密钥。 */
    private String masterKey;

    /** 单次上游调用的超时（秒）。 */
    private int requestTimeoutSeconds = 30;

    /** 进提示词的正文上限（字符）。超了按段落截断并在结果里标记。 */
    private int maxInputChars = 8000;

    /** 生成上限。产品形态是短卡片，给 1024 足够；**思考模式默认关闭**（见调用实现）。 */
    private int maxTokens = 1024;

    /** 采样温度。摘要要稳定，取低值。 */
    private double temperature = 0.3;

    /** 每用户每分钟可触发的生成次数（读缓存不计入）。 */
    private int perMinuteLimit = 6;

    /** 每用户每天可触发的生成次数。 */
    private int perDayLimit = 60;

    public String getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(String masterKey) {
        this.masterKey = masterKey;
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public int getMaxInputChars() {
        return maxInputChars;
    }

    public void setMaxInputChars(int maxInputChars) {
        this.maxInputChars = maxInputChars;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getPerMinuteLimit() {
        return perMinuteLimit;
    }

    public void setPerMinuteLimit(int perMinuteLimit) {
        this.perMinuteLimit = perMinuteLimit;
    }

    public int getPerDayLimit() {
        return perDayLimit;
    }

    public void setPerDayLimit(int perDayLimit) {
        this.perDayLimit = perDayLimit;
    }
}
