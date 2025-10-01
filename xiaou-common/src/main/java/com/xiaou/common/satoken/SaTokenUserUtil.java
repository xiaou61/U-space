package com.xiaou.common.satoken;

import lombok.extern.slf4j.Slf4j;

/**
 * Sa-Token 用户上下文工具类
 * 用于获取当前登录的管理员或用户信息
 * 
 * @author xiaou
 */
@Slf4j
public class SaTokenUserUtil {
    
    /**
     * Session 中存储用户信息的 key
     */
    private static final String USER_INFO_KEY = "userInfo";
    
    /**
     * Session 中存储用户名的 key
     */
    private static final String USERNAME_KEY = "username";
    
    /**
     * 获取当前登录的管理员信息（泛型方法）
     * 
     * @param clazz 返回类型
     * @return 管理员信息，未登录返回null
     */
    public static <T> T getCurrentAdmin(Class<T> clazz) {
        try {
            if (!StpAdminUtil.isLogin()) {
                log.debug("管理员未登录");
                return null;
            }
            return StpAdminUtil.get(USER_INFO_KEY, clazz);
        } catch (Exception e) {
            log.error("获取当前管理员信息失败", e);
            return null;
        }
    }
    
    /**
     * 获取当前登录的管理员信息（Object 类型）
     * 
     * @return 管理员信息，未登录返回null
     */
    public static Object getCurrentAdmin() {
        try {
            if (!StpAdminUtil.isLogin()) {
                log.debug("管理员未登录");
                return null;
            }
            return StpAdminUtil.get(USER_INFO_KEY);
        } catch (Exception e) {
            log.error("获取当前管理员信息失败", e);
            return null;
        }
    }
    
    /**
     * 获取当前登录的管理员ID
     * 
     * @return 管理员ID，未登录返回null
     */
    public static Long getCurrentAdminId() {
        try {
            if (!StpAdminUtil.isLogin()) {
                return null;
            }
            return StpAdminUtil.getLoginIdAsLong();
        } catch (Exception e) {
            log.error("获取当前管理员ID失败", e);
            return null;
        }
    }
    
    /**
     * 获取当前登录的用户信息（泛型方法）
     * 
     * @param clazz 返回类型
     * @return 用户信息，未登录返回null
     */
    public static <T> T getCurrentUser(Class<T> clazz) {
        try {
            if (!StpUserUtil.isLogin()) {
                log.debug("用户未登录");
                return null;
            }
            return StpUserUtil.get(USER_INFO_KEY, clazz);
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return null;
        }
    }
    
    /**
     * 获取当前登录的用户信息（Object 类型）
     * 
     * @return 用户信息，未登录返回null
     */
    public static Object getCurrentUser() {
        try {
            if (!StpUserUtil.isLogin()) {
                log.debug("用户未登录");
                return null;
            }
            return StpUserUtil.get(USER_INFO_KEY);
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return null;
        }
    }
    
    /**
     * 获取当前登录的用户ID
     * 
     * @return 用户ID，未登录返回null
     */
    public static Long getCurrentUserId() {
        try {
            if (!StpUserUtil.isLogin()) {
                return null;
            }
            return StpUserUtil.getLoginIdAsLong();
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
            return null;
        }
    }
    
    /**
     * 判断当前是否是管理员
     * 
     * @return true-是管理员，false-不是管理员
     */
    public static boolean isAdmin() {
        return StpAdminUtil.isLogin();
    }
    
    /**
     * 判断当前是否是普通用户
     * 
     * @return true-是普通用户，false-不是普通用户
     */
    public static boolean isUser() {
        return StpUserUtil.isLogin();
    }
    
    /**
     * 获取当前登录管理员的用户名
     * 
     * @return 用户名，未登录或获取失败返回 null
     */
    public static String getCurrentAdminUsername() {
        try {
            if (!StpAdminUtil.isLogin()) {
                return null;
            }
            Object username = StpAdminUtil.get(USERNAME_KEY);
            return username != null ? username.toString() : null;
        } catch (Exception e) {
            log.error("获取当前管理员用户名失败", e);
            return null;
        }
    }
    
    /**
     * 获取当前登录用户的用户名
     * 
     * @return 用户名，未登录或获取失败返回 null
     */
    public static String getCurrentUserUsername() {
        try {
            if (!StpUserUtil.isLogin()) {
                return null;
            }
            Object username = StpUserUtil.get(USERNAME_KEY);
            return username != null ? username.toString() : null;
        } catch (Exception e) {
            log.error("获取当前用户用户名失败", e);
            return null;
        }
    }
    
    /**
     * 获取当前登录管理员的用户名（带默认值）
     * 
     * @param defaultValue 默认值
     * @return 用户名，未登录或获取失败返回默认值
     */
    public static String getCurrentAdminUsername(String defaultValue) {
        String username = getCurrentAdminUsername();
        return username != null ? username : defaultValue;
    }
    
    /**
     * 获取当前登录用户的用户名（带默认值）
     * 
     * @param defaultValue 默认值
     * @return 用户名，未登录或获取失败返回默认值
     */
    public static String getCurrentUserUsername(String defaultValue) {
        String username = getCurrentUserUsername();
        return username != null ? username : defaultValue;
    }
}
