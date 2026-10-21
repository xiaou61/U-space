package io.github.xiaou61.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 缓存 token 的承载（WORK-031 TASK-002）。
 *
 * <p>起因：`ai_runs.cache_read_tokens` 与 `ai_run_spans.cache_read_tokens` 一直恒为 0，
 * 因为调用点把第三个参数写死成 0，而 `toUsage()` 根本没读 `ChatUsage.getCachedTokens()`。
 * 实测 DeepSeek 会真的报缓存命中（尖刺里见过 `cached=256`），所以这是**记录丢失**而不是"没有缓存"。
 */
class TokenUsageTest {

    @Test
    @DisplayName("三参构造默认缓存为 0：不关心缓存的调用点行为不变")
    void threeArgConstructorDefaultsCacheToZero() {
        TokenUsage usage = new TokenUsage(100, 20, 120);

        assertThat(usage.cacheReadTokens()).isZero();
        assertThat(usage.totalTokens()).isEqualTo(120);
    }

    @Test
    @DisplayName("四参构造带上缓存量，且缓存不计入 totalTokens")
    void fourArgConstructorKeepsCacheSeparateFromTotal() {
        TokenUsage usage = new TokenUsage(446, 12, 458, 256);

        assertThat(usage.cacheReadTokens()).isEqualTo(256);
        assertThat(usage.totalTokens()).isEqualTo(458);
    }
}
