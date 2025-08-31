package com.xiaou.system.utils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * JWT密钥生成器
 * 用于生成符合HS512要求的安全密钥
 *
 * @author xiaou
 */
public class JwtKeyGenerator {

    public static void main(String[] args) {
        System.out.println("=== JWT密钥生成器 ===");
        System.out.println("生成符合HS512算法要求的安全密钥\n");
        
        // 生成HS512所需的安全密钥
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        
        System.out.println("生成的密钥（Base64编码）:");
        System.out.println(base64Key);
        System.out.println("\n密钥长度: " + key.getEncoded().length + " 字节 (" + (key.getEncoded().length * 8) + " 位)");
        System.out.println("是否符合HS512要求: " + (key.getEncoded().length >= 64 ? "✅ 是" : "❌ 否"));
        
        System.out.println("\n=== 配置文件更新 ===");
        System.out.println("请将以下配置复制到 application.yml 中:");
        System.out.println("jwt:");
        System.out.println("  secret: " + base64Key);
        
        System.out.println("\n=== 简化版密钥（字符串） ===");
        // 生成一个简单的64字符密钥
        String simpleKey = "Code-Nest-JWT-Secret-Key-For-HS512-Algorithm-Security-2025-V1";
        System.out.println("简化密钥: " + simpleKey);
        System.out.println("长度: " + simpleKey.length() + " 字符 (" + (simpleKey.length() * 8) + " 位)");
        System.out.println("是否符合要求: " + (simpleKey.length() >= 64 ? "✅ 是" : "❌ 否"));
    }
} 