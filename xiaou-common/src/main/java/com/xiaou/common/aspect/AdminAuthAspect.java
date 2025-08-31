package com.xiaou.common.aspect;

import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.UserContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 管理员权限验证切面
 * 
 * @author xiaou
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAuthAspect {

    private final UserContextUtil userContextUtil;

    /**
     * 切点：标注了@RequireAdmin注解的方法
     */
    @Pointcut("@annotation(com.xiaou.common.annotation.RequireAdmin)")
    public void adminAuthPointcut() {
    }

    /**
     * 环绕通知：验证管理员权限
     */
    @Around("adminAuthPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireAdmin requireAdmin = method.getAnnotation(RequireAdmin.class);
        
        try {
            // 获取当前用户信息
            UserContextUtil.UserInfo currentUser = userContextUtil.getCurrentUser();
            
            // 检查是否已登录
            if (currentUser == null) {
                log.warn("访问需要管理员权限的接口但未登录，方法: {}.{}", 
                        method.getDeclaringClass().getSimpleName(), method.getName());
                throw new BusinessException("请先登录");
            }
            
            // 检查是否为管理员
            if (!currentUser.isAdmin()) {
                log.warn("普通用户尝试访问管理员接口，用户: {}({}), 方法: {}.{}", 
                        currentUser.getUsername(), currentUser.getUserType(),
                        method.getDeclaringClass().getSimpleName(), method.getName());
                throw new BusinessException(requireAdmin.message());
            }
            
            // 检查用户状态
            if (currentUser.getStatus() != null && currentUser.getStatus() != 0) {
                log.warn("被禁用/删除的管理员尝试访问接口，管理员: {}(状态:{}), 方法: {}.{}", 
                        currentUser.getUsername(), currentUser.getStatus(),
                        method.getDeclaringClass().getSimpleName(), method.getName());
                throw new BusinessException("账户已被禁用或删除，无法执行此操作");
            }
            
            log.debug("管理员权限验证通过，管理员: {}, 方法: {}.{}", 
                    currentUser.getUsername(),
                    method.getDeclaringClass().getSimpleName(), method.getName());
            
            // 验证通过，继续执行方法
            return joinPoint.proceed();
            
        } catch (BusinessException e) {
            // 业务异常直接抛出
            throw e;
        } catch (Exception e) {
            log.error("管理员权限验证异常，方法: {}.{}", 
                    method.getDeclaringClass().getSimpleName(), method.getName(), e);
            throw new BusinessException("权限验证失败");
        }
    }
} 