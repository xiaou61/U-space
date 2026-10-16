package io.github.xiaou61.ai.internal.settings;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 设置的读取与写入（WORK-013）。
 *
 * <p><b>没有行就是"全开"</b>：默认值写在代码里而不是靠插一行默认数据——那样每个新用户都要在
 * 注册链路里插一次，而漏插的后果是"这个人静默地没有记忆"。读取时给默认值，写入时 upsert，
 * 是这里唯一需要维持的约定。
 *
 * <p>**"关掉不删"与"清空"分得开**：{@link #disableMemory} 只翻总闸，一条记忆都不动；
 * 清空是记忆那边的动作（{@code AiMemoryController} 的 DELETE）。把两者合成一个动作，
 * 用户想"先关掉看看"就会顺手被清空历史——那是不可逆的。
 */
@Service
public class AiSettingsStore {

    private final AiUserSettingsMapper mapper;

    AiSettingsStore(AiUserSettingsMapper mapper) {
        this.mapper = mapper;
    }

    /** 三件开关的对外形状（也是 {@code PUT} 的请求体）。 */
    public record Settings(boolean memoryEnabled, boolean referenceSavedMemories, boolean referenceChatHistory) {

        /**
         * 没有设置行时的取值：**全开**。
         *
         * <p>一是现状就是"既召回又带历史"（不改变存量行为），二是记忆在默认关闭时价值为零——
         * 用户根本不会去打开一个他还不知道存在的东西。
         */
        public static Settings defaults() {
            return new Settings(true, true, true);
        }
    }

    public Settings get(long userId) {
        return mapper.find(userId)
                .map(row -> new Settings(row.isMemoryEnabled(), row.isReferenceSavedMemories(),
                        row.isReferenceChatHistory()))
                .orElseGet(Settings::defaults);
    }

    /** 整行覆盖。三个布尔一起给（{@code PUT} 的语义就是"设置成这样"）。 */
    @Transactional
    public Settings update(long userId, Settings settings) {
        mapper.upsert(userId, settings.memoryEnabled(), settings.referenceSavedMemories(),
                settings.referenceChatHistory(), Instant.now());
        return settings;
    }

    /**
     * 只关总闸，另外两个轴保持原样（"清空并关闭"用）。
     *
     * <p>读一次再写，而不是写死另外两个为 true：用户可能只想关记忆、仍然接受"引用历史"，
     * 把它一起改掉就是替他改了没让他选的东西。
     */
    @Transactional
    public Settings disableMemory(long userId) {
        Settings current = get(userId);
        return update(userId, new Settings(false, current.referenceSavedMemories(),
                current.referenceChatHistory()));
    }
}
