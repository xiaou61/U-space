package io.github.xiaou61.ai.internal.credential;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 配置（非计费部分）。
 *
 * <p>这些是"摘要这一路"的默认值：超时、正文上限、生成上限、限流。会话那一份在
 * {@link io.github.xiaou61.ai.internal.conversation.ConversationProperties}；平台凭据、价目表与
 * 额度在 {@link io.github.xiaou61.ai.internal.billing.BillingProperties}。
 *
 * <p><b>WORK-009 起没有 master-key 了</b>：那个主密钥是用来加密用户自带的模型 Key 的，而用户自带
 * Key 这条路已经撤销（上游凭据由平台在部署配置里持有）。没有秘密落库，就不需要一个保护它的密钥。
 */
@ConfigurationProperties(prefix = "paideia.ai")
public class AiProperties {

    /** 单次上游调用的超时（秒）。 */
    private int requestTimeoutSeconds = 30;

    /** 进提示词的正文上限（字符）。超了按段落截断并在结果里标记。 */
    private int maxInputChars = 8000;

    /**
     * 生成上限。**思考 token 计入这个上限**，而 deepseek-flash 默认就开思考——代码里没有任何
     * 关掉它的开关，早先注释写的"思考模式默认关闭"与实测不符（2026-10-19 点评在 1024 下思考吃掉
     * 696 token、输出被截成半截 JSON 而失败）。所以不能按"短产物"给值。
     *
     * <p>4096 与对话那一路一致：够一场面试点评的完整 JSON（实测约 1178 token）。
     */
    private int maxTokens = 4096;

    /** 采样温度。摘要要稳定，取低值。 */
    private double temperature = 0.3;

    /** 每用户每分钟可触发的生成次数（读缓存不计入）。 */
    private int perMinuteLimit = 6;

    /** 每用户每天可触发的生成次数。 */
    private int perDayLimit = 60;

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
