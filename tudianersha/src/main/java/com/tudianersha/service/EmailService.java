package com.tudianersha.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    // 存储验证码的临时Map（实际项目中应使用Redis）
    private Map<String, VerificationCode> codeStore = new HashMap<>();
    
    /**
     * 发送验证码到邮箱
     * @param email 邮箱地址
     * @return 验证码
     */
    public String sendVerificationCode(String email) {
        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        
        // 存储验证码（5分钟有效期）
        codeStore.put(email, new VerificationCode(code, System.currentTimeMillis() + 5 * 60 * 1000));
        
        try {
            // 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("途点儿啥 - 注册验证码");
            message.setText("您的验证码是：" + code + "\n\n验证码5分钟内有效，请勿泄露给他人。\n\n途点儿啥旅行规划平台");
            
            mailSender.send(message);
            System.out.println("[Email] 验证码已发送到 " + email + ": " + code);
            
        } catch (Exception e) {
            System.err.println("[Email] 发送邮件失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("邮件发送失败", e);
        }
        
        return code;
    }
    
    /**
     * 验证验证码是否正确
     * @param email 邮箱地址
     * @param code 验证码
     * @return 是否验证通过
     */
    public boolean verifyCode(String email, String code) {
        VerificationCode storedCode = codeStore.get(email);
        
        if (storedCode == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > storedCode.getExpireTime()) {
            codeStore.remove(email);
            return false;
        }
        
        // 验证码匹配
        if (storedCode.getCode().equals(code)) {
            codeStore.remove(email); // 验证成功后移除
            return true;
        }
        
        return false;
    }
    
    /**
     * 验证码内部类
     */
    private static class VerificationCode {
        private String code;
        private long expireTime;
        
        public VerificationCode(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
        
        public String getCode() {
            return code;
        }
        
        public long getExpireTime() {
            return expireTime;
        }
    }
}
