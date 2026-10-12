package io.github.xiaou61.ai;

/**
 * 调模型所需的三样东西。
 *
 * <p>它只是个**载体**：让端口不必知道凭据是怎么存、怎么解密的。里面的 {@code apiKey} 是明文，
 * 因此这个类型的使用纪律是——**只允许在"即将发起调用"这一小段里存在**，不得进日志、
 * 不得进响应、不得被长期持有（每次调用时现取现用）。
 */
public record ModelAccess(String baseUrl, String model, String apiKey) {
}
