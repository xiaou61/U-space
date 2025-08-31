package com.xiaou.system.aspect;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.hutool.json.JSONUtil;
import com.xiaou.common.annotation.Log;
import com.xiaou.system.domain.SysAdmin;
import com.xiaou.system.domain.SysOperationLog;
import com.xiaou.system.security.JwtTokenUtil;
import com.xiaou.system.service.SysOperationLogService;
import com.xiaou.system.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面
 *
 * @author xiaou
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SysOperationLogService operationLogService;
    private final JwtTokenUtil jwtTokenUtil;
    private final TokenService tokenService;

    /**
     * 切点：标注了@Log注解的方法
     */
    @Pointcut("@annotation(com.xiaou.common.annotation.Log)")
    public void logPointcut() {
    }

    /**
     * 方法执行成功后记录日志
     */
    @AfterReturning(pointcut = "logPointcut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {
        handleLog(joinPoint, null, result);
    }

    /**
     * 方法执行异常后记录日志
     */
    @AfterThrowing(pointcut = "logPointcut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    /**
     * 处理日志记录
     */
    private void handleLog(JoinPoint joinPoint, Exception e, Object result) {
        try {
            // 获取@Log注解信息
            Log logAnnotation = getLogAnnotation(joinPoint);
            if (logAnnotation == null) {
                return;
            }

            // 获取请求信息
            HttpServletRequest request = getHttpServletRequest();
            if (request == null) {
                return;
            }

            // 构建操作日志对象
            SysOperationLog operationLog = buildOperationLog(logAnnotation, joinPoint, request, e, result);
            
            // 异步保存操作日志
            operationLogService.saveOperationLog(operationLog);

        } catch (Exception ex) {
            log.error("记录操作日志异常", ex);
        }
    }

    /**
     * 获取@Log注解信息
     */
    private Log getLogAnnotation(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            Class<?> targetClass = joinPoint.getTarget().getClass();
            Class<?>[] parameterTypes = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterTypes();
            Method method = targetClass.getMethod(methodName, parameterTypes);
            return method.getAnnotation(Log.class);
        } catch (Exception e) {
            log.error("获取@Log注解信息失败", e);
            return null;
        }
    }

    /**
     * 构建操作日志对象
     */
    private SysOperationLog buildOperationLog(Log logAnnotation, JoinPoint joinPoint, 
                                              HttpServletRequest request, Exception e, Object result) {
        
        SysOperationLog operationLog = new SysOperationLog();
        
        // 基本信息
        operationLog.setOperationId(IdUtil.simpleUUID());
        operationLog.setModule(logAnnotation.module());
        operationLog.setOperationType(logAnnotation.type().name());
        operationLog.setDescription(logAnnotation.description());
        
        // 请求信息
        operationLog.setRequestUri(request.getRequestURI());
        operationLog.setRequestMethod(request.getMethod());
        operationLog.setMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        
        // 请求参数
        if (logAnnotation.saveRequestData()) {
            String requestParams = getRequestParams(joinPoint, request);
            operationLog.setRequestParams(requestParams);
        }
        
        // 响应数据
        if (logAnnotation.saveResponseData() && result != null) {
            try {
                String responseData = JSONUtil.toJsonStr(result);
                // 限制响应数据长度
                if (responseData.length() > 2000) {
                    responseData = responseData.substring(0, 2000) + "...";
                }
                operationLog.setResponseData(responseData);
            } catch (Exception ex) {
                operationLog.setResponseData("序列化响应数据失败");
            }
        }
        
        // 操作人信息
        setOperatorInfo(operationLog, request);
        
        // 客户端信息
        setClientInfo(operationLog, request);
        
        // 操作状态和错误信息
        if (e != null) {
            operationLog.setStatus(1); // 失败
            operationLog.setErrorMsg(getErrorMessage(e));
        } else {
            operationLog.setStatus(0); // 成功
        }
        
        // 操作时间
        operationLog.setOperationTime(LocalDateTime.now());
        operationLog.setCostTime(0L); // 这里可以通过计时来获取实际耗时
        
        return operationLog;
    }

    /**
     * 获取请求参数
     */
    private String getRequestParams(JoinPoint joinPoint, HttpServletRequest request) {
        try {
            StringBuilder params = new StringBuilder();
            
            // URL参数
            String queryString = request.getQueryString();
            if (StrUtil.isNotBlank(queryString)) {
                params.append("Query: ").append(queryString).append("; ");
            }
            
            // 方法参数
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                params.append("Args: ");
                for (Object arg : args) {
                    if (arg != null) {
                        try {
                            // 过滤敏感信息
                            String argStr = JSONUtil.toJsonStr(arg);
                            if (argStr.contains("password") || argStr.contains("token")) {
                                argStr = "***敏感信息已隐藏***";
                            }
                            params.append(argStr).append(", ");
                        } catch (Exception e) {
                            params.append(arg.getClass().getSimpleName()).append(", ");
                        }
                    }
                }
            }
            
            String result = params.toString();
            // 限制参数长度
            if (result.length() > 2000) {
                result = result.substring(0, 2000) + "...";
            }
            return result;
            
        } catch (Exception e) {
            return "获取请求参数失败: " + e.getMessage();
        }
    }

    /**
     * 设置操作人信息
     */
    private void setOperatorInfo(SysOperationLog operationLog, HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (StrUtil.isNotBlank(authHeader)) {
                String token = jwtTokenUtil.getTokenFromHeader(authHeader);
                if (StrUtil.isNotBlank(token)) {
                    SysAdmin admin = tokenService.getAdminFromToken(token);
                    if (admin != null) {
                        operationLog.setOperatorId(admin.getId());
                        operationLog.setOperatorName(admin.getUsername());
                        return;
                    }
                }
            }
            
            // 如果无法获取用户信息，使用默认值
            operationLog.setOperatorId(null);
            operationLog.setOperatorName("匿名用户");
            
        } catch (Exception e) {
            log.error("获取操作人信息失败", e);
            operationLog.setOperatorId(null);
            operationLog.setOperatorName("未知用户");
        }
    }

    /**
     * 设置客户端信息
     */
    private void setClientInfo(SysOperationLog operationLog, HttpServletRequest request) {
        try {
            // IP地址
            String ip = getIpAddress(request);
            operationLog.setOperatorIp(ip);
            
            // 地理位置（这里简化处理）
            operationLog.setOperationLocation("未知");
            
            // 用户代理信息
            String userAgent = request.getHeader("User-Agent");
            if (StrUtil.isNotBlank(userAgent)) {
                UserAgent ua = UserAgentUtil.parse(userAgent);
                operationLog.setBrowser(ua.getBrowser().getName() + " " + ua.getVersion());
                operationLog.setOs(ua.getOs().getName());
            } else {
                operationLog.setBrowser("未知浏览器");
                operationLog.setOs("未知系统");
            }
            
        } catch (Exception e) {
            log.error("获取客户端信息失败", e);
            operationLog.setOperatorIp("未知IP");
            operationLog.setOperationLocation("未知");
            operationLog.setBrowser("未知浏览器");
            operationLog.setOs("未知系统");
        }
    }

    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果是多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 获取错误信息
     */
    private String getErrorMessage(Exception e) {
        if (e == null) {
            return "";
        }
        
        StringBuilder errorMsg = new StringBuilder();
        errorMsg.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
        
        // 限制错误信息长度
        String result = errorMsg.toString();
        if (result.length() > 1000) {
            result = result.substring(0, 1000) + "...";
        }
        return result;
    }

    /**
     * 获取HttpServletRequest对象
     */
    private HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }
} 