package com.xiaou.secure.util;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 签名工具类
 */
public class SignUtil {

    private SignUtil() {
    }

    /**
     * 生成签名
     * 
     * @param params 不包含 sign 的参数 map，已按字典序排序
     * @param secret 秘钥
     */
    public static String sign(Map<String, String> params, String secret) {
        StringJoiner sj = new StringJoiner("&");
        params.forEach((k, v) -> sj.add(k + "=" + v));
        String data = sj.toString();
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret.getBytes(StandardCharsets.UTF_8)).hmacHex(data);
    }
}