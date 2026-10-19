package io.github.xiaou61.ai.internal.conversation;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.xiaou61.ai.Harness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 导师提示词的护栏（WORK-020 TASK-014）。
 *
 * <p>这四条不是"文案品味"，而是 PNAS 2025 那组对照实验里区分"有效"与"有害"的关键：
 * 没有护栏的通用 ChatGPT 让练习涨 48%、撤掉后考试降 17%；有护栏的涨 127%、撤掉后无损失。
 *
 * <p>断言刻意只看**存在性**：提示词是可以改写的，但改掉这几条等于拆掉护栏。语义是否真的
 * 生效无法单测（那要跑上游），所以这里是"至少没有悄悄删掉"这一档保证，已登记为已知缺口。
 */
class TutorPromptTest {

    @Test
    @DisplayName("四条硬规则必须还在提示词里")
    void guardrailsArePresent() {
        String system = TutorPrompt.system();
        assertThat(system).contains("默认不给完整答案");
        assertThat(system).contains("先要思路");
        assertThat(system).contains("提示分四级");
        assertThat(system).contains("L4");
    }

    @Test
    @DisplayName("薄弱点必须走平台算好的工具，不许模型凭印象猜")
    void weakConceptsMustComeFromTheTool() {
        assertThat(TutorPrompt.system()).contains("knowledge_weak_concepts");
    }

    @Test
    @DisplayName("版本号非空，且与通用助手不同")
    void versionIsSetAndDistinct() {
        assertThat(TutorPrompt.version()).isNotBlank();
        assertThat(TutorPrompt.version()).isNotEqualTo(ConversationPrompt.VERSION);
    }

    @Test
    @DisplayName("harness 注册表认识 tutor，未知值仍退回通用助手")
    void harnessIsRegistered() {
        assertThat(Harness.ofOrDefault("tutor")).isEqualTo(Harness.TUTOR);
        assertThat(Harness.ofOrDefault("nonsense")).isEqualTo(Harness.ASSISTANT);
        assertThat(Harness.TUTOR.key()).isEqualTo("tutor");
    }

    @Test
    @DisplayName("学情段落的措辞标明来源，避免被当成用户自述")
    void learningContextMarksProvenance() {
        String text = TutorPrompt.learningContext("状态：学习中");
        assertThat(text).contains("平台记录的事实");
        assertThat(text).contains("状态：学习中");
    }
}
