package com.xiaou.monitor.aspect;

import cn.hutool.core.util.StrUtil;
import com.xiaou.common.utils.AdminContextUtil;
import com.xiaou.common.utils.UserContextUtil;
import com.xiaou.monitor.context.MonitorContext;
import com.xiaou.monitor.context.MonitorContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * 监控切面
 * 在Controller层拦截请求，设置监控上下文
 *
 * @author xiaou
 */
@Slf4j
@Aspect
@Component
public class MonitorAspect {

    /**
     * 拦截所有Controller的方法
     */
    @Pointcut("execution(* com.xiaou.*.controller..*.*(..))")
    public void controllerPointcut() {}

    /**
     * 环绕通知，设置监控上下文
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            // 设置监控上下文
            setMonitorContext(pjp);
            
            // 执行目标方法
            return pjp.proceed();
            
        } finally {
            // 清除上下文
            MonitorContextHolder.clearContext();
            
            // 记录总执行时间
            long totalTime = System.currentTimeMillis() - startTime;
            if (totalTime > 3000) { // 超过3秒的请求记录警告
                log.warn("检测到慢请求: {}.{} - 总执行时间: {}ms", 
                    pjp.getTarget().getClass().getSimpleName(), 
                    pjp.getSignature().getName(), 
                    totalTime);
            }
        }
    }

    /**
     * 设置监控上下文
     */
    private void setMonitorContext(ProceedingJoinPoint pjp) {
        try {
            // 获取HTTP请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // 创建监控上下文
            MonitorContext context = new MonitorContext()
                .setTraceId(generateTraceId())
                .setRequestIp(getClientIp(request))
                .setRequestUri(request.getRequestURI())
                .setHttpMethod(request.getMethod())
                .setModule(getModuleName(pjp))
                .setMethod(getMethodName(pjp))
                .setStartTime(System.currentTimeMillis());
            
            // 设置用户信息
            setUserInfo(context, request);
            
            // 保存到上下文
            MonitorContextHolder.setContext(context);
            
        } catch (Exception e) {
            log.warn("设置监控上下文失败", e);
        }
    }

    /**
     * 设置用户信息
     */
    private void setUserInfo(MonitorContext context, HttpServletRequest request) {
        try {
            String path = request.getRequestURI();
            
            // 判断是管理员还是普通用户请求
            if (path.startsWith("/user/")) {
                // 普通用户请求
                if (UserContextUtil.hasUser()) {
                    context.setUserId(UserContextUtil.getCurrentUserId())
                           .setUserType("user")
                           .setUsername(UserContextUtil.getCurrentUsername());
                }
            } else {
                // 管理员请求
                if (AdminContextUtil.hasAdmin()) {
                    context.setUserId(AdminContextUtil.getCurrentAdminId())
                           .setUserType("admin")
                           .setUsername(AdminContextUtil.getCurrentAdminUsername());
                }
            }
        } catch (Exception e) {
            log.debug("获取用户信息失败: {}", e.getMessage());
        }
    }

    /**
     * 生成跟踪ID
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取模块名称
     */
    private String getModuleName(ProceedingJoinPoint pjp) {
        String className = pjp.getTarget().getClass().getSimpleName();
        if (className.endsWith("Controller")) {
            return className.substring(0, className.length() - "Controller".length());
        }
        return className;
    }

    /**
     * 获取方法名称
     */
    private String getMethodName(ProceedingJoinPoint pjp) {
        return pjp.getSignature().getName();
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 如果有多个IP，取第一个
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return StrUtil.isBlank(ip) ? "unknown" : ip;
    }
} 