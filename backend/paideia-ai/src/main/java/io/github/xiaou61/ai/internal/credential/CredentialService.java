package io.github.xiaou61.ai.internal.credential;

import io.github.xiaou61.ai.ModelAccess;
import io.github.xiaou61.ai.internal.provider.UpstreamGuard;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * 凭据的存取。
 *
 * <p>三条纪律，都有测试盯着：
 * <ol>
 *   <li><b>明文只在内存里走一趟</b>：收到 → 校验 → 加密 → 落库，之后只在调用前临时解开。</li>
 *   <li><b>回给界面的永远只有后四位</b>，任何接口都不返回 Key（连密文也不返回）。</li>
 *   <li><b>保存前先过 SSRF 校验</b>：baseUrl 会被服务端拿去发起请求，不能等到调用时才发现它是内网。</li>
 * </ol>
 */
@Service
public class CredentialService {

    private final ModelCredentialMapper mapper;
    private final CredentialCipher cipher;
    private final UpstreamGuard guard;

    CredentialService(ModelCredentialMapper mapper, CredentialCipher cipher, UpstreamGuard guard) {
        this.mapper = mapper;
        this.cipher = cipher;
        this.guard = guard;
    }

    /** 界面用的脱敏视图。**没有 Key 字段**，这是刻意的：加一个就会有人把它填上。 */
    public record CredentialView(boolean configured, String baseUrl, String model, String apiKeyLast4) {

        static CredentialView absent() {
            return new CredentialView(false, null, null, null);
        }
    }

    public CredentialView view(long userId) {
        return mapper.findByUser(userId)
                .map(it -> new CredentialView(true, it.getBaseUrl(), it.getModel(), it.getApiKeyLast4()))
                .orElseGet(CredentialView::absent);
    }

    public CredentialView save(long userId, String baseUrl, String model, String apiKey) {
        String trimmedUrl = baseUrl == null ? "" : baseUrl.trim();
        String trimmedModel = model == null ? "" : model.trim();
        String trimmedKey = apiKey == null ? "" : apiKey.trim();
        if (trimmedUrl.isEmpty() || trimmedModel.isEmpty() || trimmedKey.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "baseUrl、模型名与 API Key 都不能为空");
        }
        // 越早越好：这份 baseUrl 之后会被服务端拿去发起请求
        guard.assertAllowed(trimmedUrl);

        ModelCredential credential = new ModelCredential();
        credential.setUserId(userId);
        credential.setBaseUrl(trimmedUrl);
        credential.setModel(trimmedModel);
        credential.setApiKeyCipher(cipher.encrypt(trimmedKey));
        credential.setApiKeyLast4(last4(trimmedKey));
        mapper.upsert(credential);
        return view(userId);
    }

    public void delete(long userId) {
        mapper.deleteByUser(userId);
    }

    /** 取一份可用于调用的凭据；未配置返回空（调用方负责给出"尚未配置"的提示）。 */
    public Optional<ModelAccess> access(long userId) {
        return mapper.findByUser(userId)
                .map(it -> new ModelAccess(it.getBaseUrl(), it.getModel(), cipher.decrypt(it.getApiKeyCipher())));
    }

    private static String last4(String key) {
        return key.length() <= 4 ? key : key.substring(key.length() - 4);
    }
}
