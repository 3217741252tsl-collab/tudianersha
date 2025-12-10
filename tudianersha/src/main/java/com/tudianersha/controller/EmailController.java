package com.tudianersha.controller;

import com.tudianersha.dto.ApiResponse;
import com.tudianersha.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailController {
    
    @Autowired
    private EmailService emailService;
    
    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("邮箱地址不能为空"));
        }
        
        // 邮箱格式验证
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.ok(ApiResponse.error("邮箱格式不正确"));
        }
        
        try {
            String code = emailService.sendVerificationCode(email);
            
            // 开发环境返回验证码（生产环境应删除）
            Map<String, String> data = new HashMap<>();
            data.put("code", code);
            
            System.out.println("[Email] 返回数据 - code: " + code);
            System.out.println("[Email] 返回数据 - data: " + data);
            
            ApiResponse<Map<String, String>> response = ApiResponse.success("验证码已发送", data);
            System.out.println("[Email] ApiResponse - success: " + response.isSuccess());
            System.out.println("[Email] ApiResponse - message: " + response.getMessage());
            System.out.println("[Email] ApiResponse - data: " + response.getData());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("[Email] 发送失败: " + e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("发送失败，请稍后重试"));
        }
    }
    
    /**
     * 验证验证码
     */
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String email = request.get("email");
        String code = request.get("code");
        
        if (email == null || code == null) {
            response.put("success", false);
            response.put("message", "邮箱或验证码不能为空");
            return ResponseEntity.badRequest().body(response);
        }
        
        boolean isValid = emailService.verifyCode(email, code);
        response.put("success", isValid);
        response.put("message", isValid ? "验证成功" : "验证码错误或已过期");
        
        return ResponseEntity.ok(response);
    }
}
