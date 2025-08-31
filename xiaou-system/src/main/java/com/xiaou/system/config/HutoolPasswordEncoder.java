package com.xiaou.system.config;

import com.xiaou.common.utils.PasswordUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 基于Hutool的Spring Security密码编码器
 * 
 * @author xiaou
 */
public class HutoolPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        return PasswordUtil.encode(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return PasswordUtil.matches(rawPassword.toString(), encodedPassword);
    }
    
    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        // hutool的BCrypt已经是安全的，不需要升级
        return false;
    }
} 