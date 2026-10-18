package io.github.xiaou61.ai;

/**
 * 调模型所需的三样东西。
 *
 * <p>它只是个**载体**：让端口不必知道凭据是怎么存、怎么解密的。里面的 {@code apiKey} 是明文，
 * 因此这个类型的使用纪律是——**只允许在"即将发起调用"这一小段里存在**，不得进日志、
 * 不得进响应、不得被长期持有（每次调用时现取现用）。
 */
public record ModelAccess(String baseUrl, String model, String apiKey) {

    /**
     * record 自动生成的 toString 会带出明文 apiKey，与上面"不得进日志"的纪律直接冲突
     * （配置排障时 `log.debug("{}", access)` 是很常见的疏忽）。这里只留后四位。
     */
    @Override
    public String toString() {
        String masked = apiKey == null || apiKey.length() <= 4 ? "***" : "***" + apiKey.substring(apiKey.length() - 4);
        return "ModelAccess[baseUrl=" + baseUrl + ", model=" + model + ", apiKey=" + masked + "]";
    }
}
