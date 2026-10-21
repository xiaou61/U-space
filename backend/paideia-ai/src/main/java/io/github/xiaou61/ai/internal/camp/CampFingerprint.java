package io.github.xiaou61.ai.internal.camp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * 题干指纹（WORK-029）。
 *
 * <p>用途只有一个：**同一卷里不出重复题**。复测还有第二个用途——把诊断卷的全部题干
 * 作为"不要再出这些"传给模型，落库后再用指纹核对交集是否为空。
 *
 * <p>规范化只做三件事：去标点与空白、转小写、取前 40 字。**不做同义改写**：那需要模型，
 * 而模型参与的判重既不可复现，也会把"换了个说法的同一道题"当成两道。
 */
public final class CampFingerprint {

    /** 参与哈希的字数上限。够区分题干，又不会被长材料的尾巴带偏。 */
    private static final int STEM_CHARS = 40;

    private CampFingerprint() {
    }

    public static String of(String stem) {
        String normalized = normalize(stem);
        return sha256(normalized);
    }

    static String normalize(String stem) {
        if (stem == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < stem.length() && builder.length() < STEM_CHARS; index++) {
            char value = stem.charAt(index);
            if (Character.isLetterOrDigit(value)) {
                builder.append(value);
            }
        }
        return builder.toString().toLowerCase(Locale.ROOT);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                hex.append(Character.forDigit((item >> 4) & 0xF, 16));
                hex.append(Character.forDigit(item & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            // SHA-256 是 JDK 必备算法，走到这里说明运行环境坏了
            throw new IllegalStateException("SHA-256 不可用", exception);
        }
    }
}
