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
