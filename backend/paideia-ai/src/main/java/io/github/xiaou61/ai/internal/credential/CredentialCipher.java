package io.github.xiaou61.ai.internal.credential;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * 用户模型 Key 的加解密。
 *
 * <p>AES-GCM，每条独立 12 字节 IV，128 位 tag；存储布局 {@code iv || ciphertext || tag}。
 * 同一把 Key 两次保存得到不同密文——不泄露"两次填的是同一把"。
 *
 * <p><b>主密钥在构造期校验</b>：缺失、长度不对、非法 base64 都直接让应用启动失败，并说清是
 * 哪一种。这里刻意不做任何"兜底"：随机生成一把会让重启后全部密文作废且无人知道；
 * 降级成明文更是把安全问题变成静默的数据问题。
 *
 * <p>解密失败（换过主密钥、密文被篡改）抛的是可识别的业务异常，界面提示"重新填写"，
 * 而不是 500——用户能自己修好它。
 */
@Component
class CredentialCipher {

    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;
    private static final int KEY_BYTES = 32;
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    CredentialCipher(AiProperties properties) {
        this.key = decodeMasterKey(properties.getMasterKey());
    }

    private static SecretKeySpec decodeMasterKey(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException(
                    "缺少 paideia.ai.master-key（环境变量 PAIDEIA_AI_MASTER_KEY）："
                            + "它用于加密用户填写的模型 Key，缺失时不能降级为明文，因此拒绝启动。"
                            + "生成方式：openssl rand -base64 32");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(raw.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "paideia.ai.master-key 不是合法的 base64（应为 32 字节的 base64：openssl rand -base64 32）");
        }
        if (decoded.length != KEY_BYTES) {
            throw new IllegalStateException(
                    "paideia.ai.master-key 解出来是 " + decoded.length + " 字节，要求 " + KEY_BYTES
                            + " 字节（AES-256）");
        }
        return new SecretKeySpec(decoded, "AES");
    }

    /** 加密。返回 {@code iv || ciphertext || tag}。 */
    byte[] encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] sealed = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[iv.length + sealed.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(sealed, 0, payload, iv.length, sealed.length);
            return payload;
        } catch (GeneralSecurityException exception) {
            // 加密失败只可能是环境问题（算法不可用），不可能是用户输入问题
            throw new IllegalStateException("加密用户凭据失败", exception);
        }
    }

    /**
     * 解密。失败时抛业务异常而不是返回 null：调用方必须知道"这把 Key 用不了"，
     * 而不是拿着空串去调上游。
     */
    String decrypt(byte[] payload) {
        if (payload == null || payload.length <= IV_LENGTH) {
            throw undecryptable();
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key,
                    new GCMParameterSpec(TAG_BITS, payload, 0, IV_LENGTH));
            byte[] plaintext = cipher.doFinal(payload, IV_LENGTH, payload.length - IV_LENGTH);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw undecryptable();
        }
    }

    private static BizException undecryptable() {
        return new BizException(ErrorCode.CONFLICT,
                "已保存的凭据无法解密（服务端主密钥换过了），请重新填写你的模型 Key");
    }
}
