package com.xiaou.secure.aspect;

import com.xiaou.secure.exception.SecureException;
import com.xiaou.secure.properties.SecureProperties;
import com.xiaou.secure.util.AESUtil;
import com.xiaou.secure.util.SignUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 安全校验切面
 */
@Aspect
@Component
public class SecureAspect {

    private static final Logger log = LoggerFactory.getLogger(SecureAspect.class);

    @Autowired
    private SecureProperties properties;

    @Around("@annotation(com.xiaou.secure.annotation.SecureApi)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return pjp.proceed();
        }
        HttpServletRequest request = attrs.getRequest();

        Map<String, String> params = extractParams(request);

        // 1. 时间戳校验
        validateTimestamp(params.get("timestamp"));

        // 2. 签名校验
        validateSign(params);

        // 3. AES 解密 data 字段
        if (params.containsKey("data")) {
            String plaintext = AESUtil.decrypt(params.get("data"), properties.getAesKey());
            // 把解密后的内容放到 request attribute，方便业务层读取
            request.setAttribute("secureData", plaintext);
        }

        return pjp.proceed();
    }

    private Map<String, String> extractParams(HttpServletRequest request) throws IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, String> params = new HashMap<>();
        parameterMap.forEach((k, v) -> params.put(k, v[0]));

        // 如果没有参数，但可能是 JSON body，需要读取 body
        if (params.isEmpty() && request.getContentType() != null
                && request.getContentType().startsWith("application/json")) {
            String body = readBody(request);
            if (body != null && !body.isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    Map<String, Object> jsonMap = mapper.readValue(body, Map.class);
                    jsonMap.forEach((k, v) -> params.put(k, v == null ? null : v.toString()));
                } catch (Exception e) {
                    // 回退到原始 & 分隔的解析方式，兼容 x-www-form-urlencoded 字符串
                    Arrays.stream(body.split("&")).forEach(kv -> {
                        String[] kvArr = kv.split("=", 2);
                        if (kvArr.length == 2) {
                            params.put(kvArr[0], kvArr[1]);
                        }
                    });
                }
            }
        }
        return params;
    }

    private String readBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private void validateTimestamp(String timestampStr) {
        if (timestampStr == null) {
            throw new SecureException("timestamp missing");
        }
        long ts;
        try {
            ts = Long.parseLong(timestampStr);
        } catch (NumberFormatException e) {
            throw new SecureException("timestamp invalid");
        }
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - ts) > properties.getAllowedTimestampOffset()) {
            throw new SecureException("timestamp expired");
        }
    }

    private void validateSign(Map<String, String> params) {
        String sign = params.remove("sign");
        if (sign == null) {
            throw new SecureException("sign missing");
        }
        // 排序
        Map<String, String> sorted = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
        String expected = SignUtil.sign(sorted, properties.getSignSecret());
        if (!Objects.equals(expected, sign)) {
            throw new SecureException("sign invalid");
        }
    }
}