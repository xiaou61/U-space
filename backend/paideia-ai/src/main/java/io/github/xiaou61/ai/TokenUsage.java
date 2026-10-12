package io.github.xiaou61.ai;

/**
 * 一次真实调用的 token 用量（用户裁决：**返还给用户看**）。
 *
 * <p>命中缓存时没有用量——那不是"零消耗"，而是"本次没有产生调用"，所以缓存命中的响应里
 * 这个字段是 {@code null} 而不是 0：把两者混成同一个值会让用户以为又付了一次钱。
 */
public record TokenUsage(int inputTokens, int outputTokens, int totalTokens) {
}
