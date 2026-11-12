package com.example.controller;

import com.example.entity.User;
import com.example.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 使用@RequestParam接收URL参数
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "password", required = false) String password) {

        // 1. 验证必需字段
        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(createErrorResponse("用户名和密码不能为空"));
        }

        // 2. 进行用户认证
        User user = authService.authenticate(username, password);

        if (user == null) {
            return ResponseEntity.status(401).body(createErrorResponse("用户名或密码错误"));
        }

        // 3. 创建成功响应
        return ResponseEntity.ok(createSuccessResponse(user));
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, Object> createSuccessResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("username", user.getUsername());
        response.put("fullName", user.getFullName());
        response.put("role", user.getRole());
        return response;
    }

}