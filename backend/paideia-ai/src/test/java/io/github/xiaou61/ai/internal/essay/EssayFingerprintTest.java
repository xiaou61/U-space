package io.github.xiaou61.ai.internal.essay;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 题目指纹（WORK-025）。
 *
 * <p>去重全靠它，所以必须验：**同一道题的不同写法要落到同一个指纹**，不同题目不能撞。
 * 如果这里错了，要么每天制造重复内容，要么把不同的题合并掉。
 */
class EssayFingerprintTest {

    @Test
    void ignoresNumberingPunctuationAndCase() {
        String a = EssayCollectionService.fingerprint("1、HashMap 为什么线程不安全？");
        String b = EssayCollectionService.fingerprint("HashMap为什么线程不安全");
        String c = EssayCollectionService.fingerprint("3. hashmap 为什么线程不安全");
        assertThat(a).isEqualTo(b).isEqualTo(c);
    }

    @Test
    void differentQuestionsDoNotCollide() {
        String a = EssayCollectionService.fingerprint("HashMap 为什么线程不安全");
        String b = EssayCollectionService.fingerprint("ConcurrentHashMap 为什么线程安全");
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void chineseAndEnglishQuotesAreNormalised() {
        String a = EssayCollectionService.fingerprint("什么是“双亲委派”模型");
        String b = EssayCollectionService.fingerprint("什么是双亲委派模型");
        assertThat(a).isEqualTo(b);
    }

    @Test
    void producesStableHash() {
        // 固定输入的输出必须稳定：它写进库，换了实现就会让存量指纹全部失配。
        assertThat(EssayCollectionService.fingerprint("HashMap 为什么线程不安全"))
                .isEqualTo(EssayCollectionService.fingerprint("HashMap 为什么线程不安全"))
                .hasSize(64);
    }
}
