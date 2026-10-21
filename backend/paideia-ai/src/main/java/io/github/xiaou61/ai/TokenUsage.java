package io.github.xiaou61.ai;

/**
 * 一次真实调用的 token 用量（用户裁决：**返还给用户看**）。
 *
 * <p>命中缓存时没有用量——那不是"零消耗"，而是"本次没有产生调用"，所以缓存命中的响应里
 * 这个字段是 {@code null} 而不是 0：把两者混成同一个值会让用户以为又付了一次钱。
 *
 * <p>{@code cacheReadTokens} 是上游报的**缓存命中**量（WORK-031）。它不计入
 * {@code totalTokens}——后者是上游给的原始总数，动它会让账实相符的断言失去意义。
 * 三参构造保留给不关心缓存的调用点。
 */
public record TokenUsage(int inputTokens, int outputTokens, int totalTokens, int cacheReadTokens) {

    public TokenUsage(int inputTokens, int outputTokens, int totalTokens) {
        this(inputTokens, outputTokens, totalTokens, 0);
    }
}
