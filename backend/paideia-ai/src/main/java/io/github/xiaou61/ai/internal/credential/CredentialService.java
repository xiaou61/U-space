package io.github.xiaou61.ai.internal.credential;

import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.internal.billing.BillingProperties;
import io.github.xiaou61.ai.internal.provider.UpstreamGuard;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * 凭据的来源。
 *
 * <p><b>WORK-009 起，AI 的上游凭据由平台统一持有</b>（放配置、不入库）。用户不再自带 Key，因此这里
 * 不再有"保存 / 删除"这类写路径，也不再加密任何东西——**没有秘密要落在库里**。
 *
 * <p><b>为什么 {@code userId} 参数还在</b>：凭据是"在这个用户的上下文里解析"的。将来若允许个别用户
 * 自带 Key 作为例外，这里是唯一的接入点，而两个调用点（会话与精简）不必再改一次。
 *
 * <p>明文 Key 只在构造 {@link ModelAccess} 这一刻从配置里读出来，随这次调用走完即丢——与 WORK-006
 * 定下的纪律一致（不进日志、不进响应、不被长期持有）。
 */
@Service
public class CredentialService {

    private final BillingProperties billing;

    CredentialService(BillingProperties billing, UpstreamGuard guard) {
        this.billing = billing;
        BillingProperties.PlatformCredential credential = billing.getPlatformCredential();
        if (credential != null && credential.configured()) {
            // 启动时校验一次：这份 baseUrl 之后会被服务端拿去发请求，配成内网应当立刻可见。
            // 运行期还有 GuardedHttpTransport 兜第二道（它能看见最终拼出来的地址）。
            guard.assertAllowed(credential.getBaseUrl());
        }
    }

    /**
     * 界面用的视图。
     *
     * <p>**连末四位都不给**：平台 Key 不是用户的 Key，把它的任何片段放到用户界面上都没有意义，
     * 只会多一处需要解释的东西。用户界面需要知道的只有"配没配"。
     */
    public record CredentialView(boolean configured, String baseUrl, String model) {
    }

    public CredentialView view() {
        BillingProperties.PlatformCredential credential = billing.getPlatformCredential();
        if (credential == null || !credential.configured()) {
            return new CredentialView(false, null, null);
        }
        return new CredentialView(true, credential.getBaseUrl(), credential.getModel());
    }

    /** 取一份可用于调用的凭据；平台未配置时返回空（调用方给出"平台尚未配置"的提示）。 */
    public Optional<ModelAccess> access(long userId) {
        BillingProperties.PlatformCredential credential = billing.getPlatformCredential();
        if (credential == null || !credential.configured()) {
            return Optional.empty();
        }
        return Optional.of(new ModelAccess(credential.getBaseUrl(), credential.getModel(), credential.getApiKey()));
    }
}
