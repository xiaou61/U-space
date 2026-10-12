package io.github.xiaou61.ai.internal.credential;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.xiaou61.platform.BizException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * 凭据加解密。
 *
 * <p>三件事必须成立：能来回、两次加密结果不同（同一把 Key 不泄露"填过两次"）、
 * 密文被改一个字节就解不开（GCM 的完整性），以及**主密钥缺失/坏掉时拒绝启动**——
 * 后者是"不静默降级成明文"的具体体现。
 */
class CredentialCipherTest {

    private static final String VALID_KEY = base64Key("paideia-test-ai-master-key-00001");

    private static String base64Key(String raw) {
        byte[] bytes = raw.getBytes(StandardCharsets.UTF_8);
        assertThat(bytes).hasSize(32);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static CredentialCipher cipher(String masterKey) {
        AiProperties properties = new AiProperties();
        properties.setMasterKey(masterKey);
        return new CredentialCipher(properties);
    }

    @Test
    @DisplayName("加密后能解回原文")
    void roundTrip() {
        CredentialCipher cipher = cipher(VALID_KEY);
        byte[] payload = cipher.encrypt("sk-abcdef123456");
        assertThat(payload).hasSizeGreaterThan(12);
        assertThat(cipher.decrypt(payload)).isEqualTo("sk-abcdef123456");
    }

    @Test
    @DisplayName("同一把 Key 两次加密得到不同密文")
    void differentCiphertextForSameInput() {
        CredentialCipher cipher = cipher(VALID_KEY);
        assertThat(cipher.encrypt("sk-same")).isNotEqualTo(cipher.encrypt("sk-same"));
    }

    @Test
    @DisplayName("密文被篡改就解不开，且抛出的是可识别的业务异常")
    void detectsTampering() {
        CredentialCipher cipher = cipher(VALID_KEY);
        byte[] payload = cipher.encrypt("sk-abcdef");
        payload[payload.length - 1] ^= 0x01;
        assertThatThrownBy(() -> cipher.decrypt(payload))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("重新填写");
    }

    @Test
    @DisplayName("换过主密钥后旧密文解不开（提示重填，不是 500）")
    void failsAfterMasterKeyRotation() {
        byte[] payload = cipher(VALID_KEY).encrypt("sk-abcdef");
        assertThatThrownBy(() -> cipher(base64Key("paideia-test-ai-master-key-00002")).decrypt(payload))
                .isInstanceOf(BizException.class);
    }

    @Test
    @DisplayName("主密钥缺失或长度不对时拒绝启动")
    void refusesToStartWithoutUsableMasterKey() {
        assertThatThrownBy(() -> cipher(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("master-key");
        assertThatThrownBy(() -> cipher(""))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> cipher("不是base64"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("base64");
        assertThatThrownBy(() -> cipher(Base64.getEncoder().encodeToString("太短".getBytes(StandardCharsets.UTF_8))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("字节");
    }
}
