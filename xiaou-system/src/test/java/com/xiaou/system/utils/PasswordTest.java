package com.xiaou.system.utils;

import com.xiaou.common.utils.PasswordUtil;
import com.xiaou.system.config.HutoolPasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密测试类
 * 用于演示和测试hutool的密码加密功能
 *
 * @author xiaou
 */
public class PasswordTest {

    @Test
    public void testPasswordEncryption() {
        System.out.println("=== Hutool密码加密测试 ===");
        
        // 测试常用密码
        String[] testPasswords = {"123456"};
        
        for (String password : testPasswords) {
            System.out.println("\n--- 测试密码: " + password + " ---");
            
            // 使用PasswordUtil直接加密
            String encoded1 = PasswordUtil.encode(password);
            String encoded2 = PasswordUtil.encode(password);
            
            System.out.println("原始密码: " + password);
            System.out.println("加密结果1: " + encoded1);
            System.out.println("加密结果2: " + encoded2);
            System.out.println("两次加密是否相同: " + encoded1.equals(encoded2));
            
            // 验证密码
            boolean matches1 = PasswordUtil.matches(password, encoded1);
            boolean matches2 = PasswordUtil.matches(password, encoded2);
            System.out.println("密码验证1: " + matches1);
            System.out.println("密码验证2: " + matches2);
            
            // 错误密码验证
            boolean wrongMatch = PasswordUtil.matches("wrongpassword", encoded1);
            System.out.println("错误密码验证: " + wrongMatch);
        }
    }

} 