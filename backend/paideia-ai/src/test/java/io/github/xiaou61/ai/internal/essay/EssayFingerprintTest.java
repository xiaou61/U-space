package io.github.xiaou61.ai.internal.essay;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * 去重指纹（WORK-025）。
 *
 * <p>**这里记录了一次线上事故的教训**：最初按"AI 生成的题目标题"算指纹，理由是
 * "同一道题会在不同面经里反复出现"。但模型每次复述措辞会变——
 * 「线程池中如何可靠地传递 ThreadLocal 上下文？」与
 * 「在线程池中如何可靠地把父线程的 ThreadLocal 上下文传给子线程？」意思相同、文本不同，
 * 指纹也就不同，于是**每次运行都新增一条**（实测重启一次，库里 4 条变 8 条）。
 *
 * <p>现在按**来源帖子 id** 算：那一列由上游给定，不会漂移。
 */
class EssayFingerprintTest {

    private static FetchedPost post(String externalId) {
        return new FetchedPost(externalId, "标题", "正文", "https://example.com/" + externalId, 1L);
    }

    @Test
    void samePostAlwaysProducesTheSameFingerprint() {
        // 这是核心保证：同一篇帖子跑多少次，指纹都不变——所以不会重复入库。
        assertThat(EssayCollectionService.fingerprintOfPost(post("2928416")))
                .isEqualTo(EssayCollectionService.fingerprintOfPost(post("2928416")))
                .hasSize(64);
    }

    @Test
    void differentPostsDoNotCollide() {
        assertThat(EssayCollectionService.fingerprintOfPost(post("2928416")))
                .isNotEqualTo(EssayCollectionService.fingerprintOfPost(post("2928961")));
    }

    @Test
    void rewordedTitleDoesNotChangeTheFingerprint() {
        // 模拟"同一篇帖子、两次运行抽出的标题措辞不同"——指纹必须仍然一致。
        FetchedPost first = new FetchedPost("2928416", "北京小厂Java一面", "正文A",
                "https://example.com/2928416", 1L);
        FetchedPost second = new FetchedPost("2928416", "北京小厂Java一面（10.18）", "正文B",
                "https://example.com/2928416", 2L);
        assertThat(EssayCollectionService.fingerprintOfPost(first))
                .isEqualTo(EssayCollectionService.fingerprintOfPost(second));
    }

    @Test
    void fingerprintIsPrefixedBySourceSoMultipleSitesCannotCollide() {
        // 前缀区分来源：将来接第二个站时，不同站的同名 id 不会撞。
        assertThat(EssayCollectionService.fingerprintOfPost(post("123")))
                .isNotEqualTo(EssayCollectionService.fingerprintOfPost(post("123-other-site")));
    }
}
