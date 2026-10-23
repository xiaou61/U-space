package io.github.xiaou61.knowledge.internal.file;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * BKT 掌握度的紧急回退开关（WORK-033 AC-010）。
 *
 * <p>AC-010 预设"关闭新实现开关后回退旧口径"，而 2026-10-22 的实现只做了**按数据自动降级**
 * （有逐题作答用 BKT、没有就回退旧加权平均）。那解决了"新用户会不会看到怪数"，
 * 但没解决"算法出问题时怎么一键退回去"——两者是不同的问题，这个开关补的是后者。
 *
 * <p>语义：{@code false} 时 `ConceptGraphService` **不查** BKT 状态，于是每个概念都走
 * `state == null` 的回退分支。也就是说关掉 = 全部概念用旧公式，即使有逐题作答。
 */
class MasteryBktSwitchTest {

    /**
     * **默认必须是开**。
     *
     * <p>如果默认关掉，等于新功能上线即关闭——而 WORK-033 的全部价值都建立在
     * 逐题作答能影响掌握度上。这条守住"默认开"这个前提。
     */
    @Test
    @DisplayName("默认启用 BKT 掌握度")
    void enabledByDefault() {
        assertThat(new KnowledgeStorageProperties().isMasteryBktEnabled())
                .as("默认应为 true，否则 WORK-033 的掌握度链路等于没上线")
                .isTrue();
    }

    /** 关掉之后必须能被读到——这是它作为开关的前提。 */
    @Test
    @DisplayName("可以关闭")
    void canBeDisabled() {
        KnowledgeStorageProperties properties = new KnowledgeStorageProperties();
        properties.setMasteryBktEnabled(false);
        assertThat(properties.isMasteryBktEnabled()).isFalse();
    }

    /** 开关是布尔而不是字符串：拼错的配置值会让应用起不来，而不是静默走错分支。 */
    @Test
    @DisplayName("属性是布尔类型")
    void isBoolean() {
        KnowledgeStorageProperties properties = new KnowledgeStorageProperties();
        properties.setMasteryBktEnabled(true);
        assertThat(properties.isMasteryBktEnabled()).isTrue();
    }
}
