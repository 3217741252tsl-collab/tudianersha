package com.tudianersha.controller;

import com.tudianersha.dto.ApiResponse;
import com.tudianersha.entity.User;
import com.tudianersha.service.UserService;
import com.tudianersha.util.PasswordEncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(user.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createUser(@RequestBody User user) {
        System.out.println("[Register] 收到注册请求: " + user.getUsername() + ", " + user.getEmail());
        
        // 检查用户名是否已存在
        if (userService.existsByUsername(user.getUsername())) {
            System.out.println("[Register] 用户名已存在: " + user.getUsername());
            return ResponseEntity.ok(ApiResponse.error("用户名已存在"));
        }
        
        // 检查邮箱是否已存在
        if (userService.existsByEmail(user.getEmail())) {
            System.out.println("[Register] 邮箱已存在: " + user.getEmail());
            return ResponseEntity.ok(ApiResponse.error("邮箱已被注册"));
        }
        
        try {
            // 加密密码
            String encodedPassword = PasswordEncoderUtil.encode(user.getPassword());
            user.setPassword(encodedPassword);
            
            // 保存用户
            User savedUser = userService.saveUser(user);
            System.out.println("[Register] 注册成功: " + savedUser.getUsername());
            
            // 返回用户信息（不包含密码）
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", savedUser.getId());
            userData.put("username", savedUser.getUsername());
            userData.put("email", savedUser.getEmail());
            
            return ResponseEntity.ok(ApiResponse.success("注册成功", userData));
            
        } catch (Exception e) {
            System.err.println("[Register] 注册失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("注册失败，请稍后重试"));
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> credentials) {
        String account = credentials.get("account"); // 用户名或邮箱
        String password = credentials.get("password");
        
        System.out.println("[Login] 登录尝试: " + account);
        
        // 查找用户（支持用户名或邮箱登录）
        Optional<User> userOpt = userService.findByUsername(account);
        if (!userOpt.isPresent()) {
            userOpt = userService.findByEmail(account);
        }
        
        if (!userOpt.isPresent()) {
            System.out.println("[Login] 用户不存在: " + account);
            return ResponseEntity.ok(ApiResponse.error("用户名或密码错误"));
        }
        
        User user = userOpt.get();
        
        // 调试信息：输出数据库中的密码
        System.out.println("[Login] 数据库密码: " + user.getPassword());
        System.out.println("[Login] 输入密码: " + password);
        System.out.println("[Login] 密码匹配结果: " + PasswordEncoderUtil.matches(password, user.getPassword()));
        
        // 验证密码
        if (!PasswordEncoderUtil.matches(password, user.getPassword())) {
            System.out.println("[Login] 密码错误: " + account);
            return ResponseEntity.ok(ApiResponse.error("用户名或密码错误"));
        }
        
        System.out.println("[Login] 登录成功: " + user.getUsername());
        
        // 返回用户信息（不包含密码）
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        
        return ResponseEntity.ok(ApiResponse.success("登录成功", userData));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateUser(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        Optional<User> userOpt = userService.getUserById(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
        
        User existingUser = userOpt.get();
        
        // 只更新用户名（如果提供了）
        if (updates.containsKey("username")) {
            String newUsername = updates.get("username");
            // 检查新用户名是否已被其他用户使用
            Optional<User> existingWithName = userService.findByUsername(newUsername);
            if (existingWithName.isPresent() && !existingWithName.get().getId().equals(id)) {
                return ResponseEntity.ok(ApiResponse.error("用户名已被占用"));
            }
            existingUser.setUsername(newUsername);
        }
        
        User updatedUser = userService.saveUser(existingUser);
        
        // 返回更新后的用户信息
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", updatedUser.getId());
        userData.put("username", updatedUser.getUsername());
        userData.put("email", updatedUser.getEmail());
        
        return ResponseEntity.ok(ApiResponse.success("更新成功", userData));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}