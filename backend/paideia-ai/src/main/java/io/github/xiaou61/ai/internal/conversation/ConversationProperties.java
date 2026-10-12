package io.github.xiaou61.ai.internal.conversation;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 助手会话的配置。
 *
 * <p>三个数值都是"实现选择"（见 WORK-006 design 的「待决定事项」），取值原则是够用且不无限增长：
 * 上下文窗口小、存储上限大、过期走滑动。
 *
 * <p>{@code ttl} 用 {@link Duration}，Spring 的宽松绑定接受 {@code 7d} / {@code 12h} 这类写法。
 */
@ConfigurationProperties(prefix = "paideia.ai.conversation")
public class ConversationProperties {

    /** 进模型上下文的消息条数上限（"看得见多少历史"）。 */
    private int maxContextMessages = 20;

    /** 单条会话在库里保留的消息条数上限；超出即删最旧的（"库里留多少"）。 */
    private int maxStoredMessages = 200;

    /** 会话有效期，自最后一条消息起算（滑动）。 */
    private Duration ttl = Duration.ofDays(7);

    /**
     * 单轮生成的 token 上限。
     *
     * <p>**刻意远大于精简那份（1024）**：本项会开思考，而 DeepSeek 的思考 token 计入
     * {@code max_tokens}，给不足会**静默返回空内容**（只在思考、正文为空）。多轮对话的正文也比
     * 一张卡片长。给足是这条已知坑的唯一对策。
     */
    private int maxTokens = 4096;

    /** 单轮调用的超时（秒）。流式下这是"多久没有新片段就判失败"的阈值。 */
    private int requestTimeoutSeconds = 60;

    /** 采样温度。对话比摘要需要一点变化，取中间值。 */
    private double temperature = 0.6;

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxContextMessages() {
        return maxContextMessages;
    }

    public void setMaxContextMessages(int maxContextMessages) {
        this.maxContextMessages = maxContextMessages;
    }

    public int getMaxStoredMessages() {
        return maxStoredMessages;
    }

    public void setMaxStoredMessages(int maxStoredMessages) {
        this.maxStoredMessages = maxStoredMessages;
    }

    public Duration getTtl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }
}
