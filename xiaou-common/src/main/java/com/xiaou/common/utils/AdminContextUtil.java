package com.xiaou.common.utils;

import com.xiaou.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 管理员上下文工具类
 * 专门用于获取当前管理员信息的便捷工具
 * 
 * @author xiaou
 */
@Component
@RequiredArgsConstructor
public class AdminContextUtil {

    private final UserContextUtil userContextUtil;

    /**
     * 获取当前管理员信息
     * 
     * @return 管理员信息
     * @throws BusinessException 如果未登录或不是管理员
     */
    public UserContextUtil.UserInfo getCurrentAdmin() {
        UserContextUtil.UserInfo currentUser = userContextUtil.getCurrentUser();
        
        if (currentUser == null) {
            throw new BusinessException("请先登录");
        }
        
        if (!currentUser.isAdmin()) {
            throw new BusinessException("需要管理员权限");
        }
        
        return currentUser;
    }

    /**
     * 获取当前管理员ID
     * 
     * @return 管理员ID
     * @throws BusinessException 如果未登录或不是管理员
     */
    public Long getCurrentAdminId() {
        return getCurrentAdmin().getId();
    }

    /**
     * 获取当前管理员用户名
     * 
     * @return 管理员用户名
     * @throws BusinessException 如果未登录或不是管理员
     */
    public String getCurrentAdminUsername() {
        return getCurrentAdmin().getUsername();
    }

    /**
     * 检查当前用户是否为管理员
     * 
     * @return 是否为管理员
     */
    public boolean isCurrentUserAdmin() {
        try {
            UserContextUtil.UserInfo currentUser = userContextUtil.getCurrentUser();
            return currentUser != null && currentUser.isAdmin();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 安全获取当前管理员ID（不抛异常）
     * 
     * @return 管理员ID，如果不是管理员则返回null
     */
    public Long getCurrentAdminIdSafely() {
        try {
            return getCurrentAdminId();
        } catch (Exception e) {
            return null;
        }
    }
} 