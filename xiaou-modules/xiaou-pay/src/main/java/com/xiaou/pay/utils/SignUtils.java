package com.xiaou.pay.utils;

import java.security.MessageDigest;
import java.util.*;

public class SignUtils {

    public static String generateSign(Map<String, String> params, String key) {
        // 1. 过滤 sign, sign_type 和空值参数
        Map<String, String> filteredParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            if (v == null || v.isEmpty()) continue;
            if ("sign".equalsIgnoreCase(k) || "sign_type".equalsIgnoreCase(k)) continue;
            filteredParams.put(k, v);
        }

        // 2. 参数名ASCII码从小到大排序
        List<String> keys = new ArrayList<>(filteredParams.keySet());
        Collections.sort(keys);

        // 3. 拼接成URL键值对格式 key=value&key2=value2
        StringBuilder sb = new StringBuilder();
        for (String k : keys) {
            sb.append(k).append("=").append(filteredParams.get(k)).append("&");
        }
        // 去掉最后一个&符号
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        // 4. 拼接密钥
        sb.append(key);

        // 5. MD5加密，转小写
        return md5(sb.toString()).toLowerCase();
    }

    private static String md5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
