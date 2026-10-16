package io.github.xiaou61.ai.internal.credential;

import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.internal.billing.BillingProperties;
import io.github.xiaou61.ai.internal.provider.UpstreamGuard;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CredentialService.class);

    private final BillingProperties billing;

    CredentialService(BillingProperties billing, UpstreamGuard guard) {
        this.billing = billing;
        BillingProperties.PlatformCredential credential = billing.getPlatformCredential();
        if (credential != null && credential.configured()) {
            // 启动时校验一次：这份 baseUrl 之后会被服务端拿去发请求，配成内网应当立刻可见。
            // 运行期还有 GuardedHttpTransport 兜第二道（它能看见最终拼出来的地址）。
            guard.assertAllowed(credential.getBaseUrl());
            // 打一行"生效的是哪一套"，让"平台配没配"在部署后一眼可见。
            // **只打 model 与 baseUrl，绝不打印 Key**（连截断也不打）：它是部署凭据，
            // 日志会被翻、会被转存，能省一次暴露就省一次。
            log.info("ai.billing.credential model={} baseUrl={} enabled={}",
                    credential.getModel(), credential.getBaseUrl(), billing.isEnabled());
        } else {
            // 不 fail fast：凭据缺失只让 AI 功能给出"平台尚未配置"的提示，不该拦住整个应用起来
            log.warn("ai.billing.credential 未配置：AI 功能会提示「平台尚未配置 AI 服务」");
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
