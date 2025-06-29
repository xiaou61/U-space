package com.xiaou.common.utils;

import cn.hutool.extra.servlet.JakartaServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NoArgsConstructor;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@NoArgsConstructor
public class ServletUtils extends JakartaServletUtil {

    /**
     * 获取当前 HTTP 请求对象
     *
     * @return 当前 HTTP 请求对象
     */
    public static HttpServletRequest getRequest() {
        try {
            return getRequestAttributes().getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前请求的请求属性
     *
     * @return {@link ServletRequestAttributes} 请求属性对象
     */
    public static ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端 IP 地址（自动获取当前请求）
     *
     * @return 客户端 IP 地址
     */
    public static String getClientIP() {
        String ip = getClientIP(getRequest());

        // 如果是 IPv6 本地地址，替换为 localhost（或 127.0.0.1）
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1"; // 或者 return "localhost";
        }
        return ip;
    }

}
