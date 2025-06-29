package com.xiaou.common.utils;

import org.springframework.util.DigestUtils;

/**
 * 密码加密工具类
 */
public class PasswordUtil {

    // 盐值常量
    private static final String SALT = "uspace";

    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    public static String getEncryptPassword(String userPassword) {
        if (userPassword == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }
}
