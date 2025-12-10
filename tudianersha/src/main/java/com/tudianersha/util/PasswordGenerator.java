package com.tudianersha.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 临时密码生成工具 - 用于生成测试账号的加密密码
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("=== 生成测试账号密码 ===");
        
        // 生成 admin123 的加密密码
        String admin123 = encoder.encode("admin123");
        System.out.println("admin123 加密后: " + admin123);
        
        // 生成 user123 的加密密码
        String user123 = encoder.encode("user123");
        System.out.println("user123 加密后: " + user123);
        
        // 生成 tsl123 的加密密码
        String tsl123 = encoder.encode("tsl123");
        System.out.println("tsl123 加密后: " + tsl123);
        
        // 生成 qjq123 的加密密码
        String qjq123 = encoder.encode("qjq123");
        System.out.println("qjq123 加密后: " + qjq123);
        
        // 生成 ysy123 的加密密码
        String ysy123 = encoder.encode("ysy123");
        System.out.println("ysy123 加密后: " + ysy123);
        
        System.out.println("\n=== 验证密码 ===");
        // 验证一下
        System.out.println("验证 admin123: " + encoder.matches("admin123", admin123));
    }
}
