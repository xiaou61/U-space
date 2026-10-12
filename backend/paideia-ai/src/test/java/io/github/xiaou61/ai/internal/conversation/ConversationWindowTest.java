package io.github.xiaou61.ai.internal.conversation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 会话窗口的裁剪规则。
 *
 * <p>两条边界各自要咬住：① 上下文裁剪只影响"看得见多少历史"，顺序必须仍是旧→新，
 * 否则模型会收到颠倒的对话；② 存储裁剪在**配置写错**（上限 &lt; 1）时什么都不删——
 * 把 0 解释成"全删"会让一个手误清空用户历史。
 */
class ConversationWindowTest {

    @Test
    @DisplayName("窗口小于总量时取最后 N 条，顺序仍是旧→新")
    void keepsNewestAndPreservesOrder() {
        List<String> oldestFirst = List.of("m1", "m2", "m3", "m4", "m5");

        assertThat(ConversationWindow.lastMessages(oldestFirst, 2)).containsExactly("m4", "m5");
    }

    @Test
    @DisplayName("窗口不小于总量时原样返回，且不改动入参")
    void keepsEverythingWhenWindowIsLargeEnough() {
        List<String> oldestFirst = List.of("m1", "m2");

        assertThat(ConversationWindow.lastMessages(oldestFirst, 2)).containsExactly("m1", "m2");
        assertThat(ConversationWindow.lastMessages(oldestFirst, 99)).containsExactly("m1", "m2");
    }

    @Test
    @DisplayName("空列表与非正窗口都返回空，不抛异常")
    void handlesEmptyAndNonPositiveWindow() {
        assertThat(ConversationWindow.lastMessages(List.of(), 5)).isEmpty();
        assertThat(ConversationWindow.lastMessages(List.of("m1"), 0)).isEmpty();
        assertThat(ConversationWindow.lastMessages(List.of("m1"), -3)).isEmpty();
    }

    @Test
    @DisplayName("存储超出上限时，只删超出部分")
    void dropsOnlyTheExcess() {
        assertThat(ConversationWindow.messagesToDrop(200, 200)).isZero();
        assertThat(ConversationWindow.messagesToDrop(201, 200)).isEqualTo(1);
        assertThat(ConversationWindow.messagesToDrop(260, 200)).isEqualTo(60);
        assertThat(ConversationWindow.messagesToDrop(10, 200)).isZero();
    }

    @Test
    @DisplayName("上限配错（< 1）时什么都不删，而不是把历史清空")
    void treatsNonPositiveLimitAsNoLimit() {
        assertThat(ConversationWindow.messagesToDrop(500, 0)).isZero();
        assertThat(ConversationWindow.messagesToDrop(500, -1)).isZero();
    }
}
