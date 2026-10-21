package io.github.xiaou61.ai.internal.digest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** URL 规范化与指纹（WORK-027 TASK-002）。 */
class DigestFingerprintTest {

    @Test
    @DisplayName("同一文章带六种追踪参数得到同一个指纹")
    void trackingParametersDoNotChangeTheFingerprint() {
        String base = "https://www.qbitai.com/2026/10/claude-new-model.html";
        String expected = DigestFingerprint.of(base);

        String[] decorated = {
            base + "?utm_source=wechat",
            base + "?utm_medium=social&utm_campaign=1021",
            base + "?from=rss",
            base + "?spm=a1z.2&fbclid=xyz",
            base + "?gclid=abc&ref=homepage",
            base + "?utm_id=1&utm_term=ai&utm_content=card&mc_cid=9&mc_eid=8",
        };

        for (String url : decorated) {
            assertThat(DigestFingerprint.of(url))
                    .as("带追踪参数的变体应与原文同指纹：%s", url)
                    .isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("不同文章得到不同指纹")
    void differentArticlesDiffer() {
        assertThat(DigestFingerprint.of("https://example.com/a"))
                .isNotEqualTo(DigestFingerprint.of("https://example.com/b"));
    }

    @Test
    @DisplayName("无查询参数的 URL 原样通过")
    void plainUrlPassesThrough() {
        assertThat(DigestFingerprint.normalize("https://example.com/posts/1"))
                .isEqualTo("https://example.com/posts/1");
    }

    @Test
    @DisplayName("末尾斜杠、host 大小写、fragment 都不改变指纹")
    void cosmeticDifferencesNormalize() {
        String expected = DigestFingerprint.of("https://example.com/posts/1");
        assertThat(DigestFingerprint.of("https://example.com/posts/1/")).isEqualTo(expected);
        assertThat(DigestFingerprint.of("https://EXAMPLE.com/posts/1")).isEqualTo(expected);
        assertThat(DigestFingerprint.of("https://example.com/posts/1#comments")).isEqualTo(expected);
    }

    @Test
    @DisplayName("**非追踪参数必须保留**——否则不同文章会被误判成同一条")
    void realQueryParametersAreKept() {
        // 带 id 的链接是不同文章，不能因为"剥参数"就合并
        assertThat(DigestFingerprint.of("https://example.com/p?id=1"))
                .isNotEqualTo(DigestFingerprint.of("https://example.com/p?id=2"));
        assertThat(DigestFingerprint.normalize("https://example.com/p?id=1&utm_source=x"))
                .isEqualTo("https://example.com/p?id=1");
    }

    @Test
    @DisplayName("解析不了的 URL 退回原文，不抛异常")
    void unparsableUrlFallsBackToRawText() {
        String weird = "不是一个链接";
        assertThat(DigestFingerprint.normalize(weird)).isEqualTo(weird);
        assertThat(DigestFingerprint.of(weird)).hasSize(64);
    }
}
