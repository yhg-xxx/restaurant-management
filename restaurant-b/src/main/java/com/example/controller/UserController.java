package com.example.controller;

import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 获取用户信息
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable int userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            // 清除敏感信息
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 更新用户信息
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserInfo(
            @PathVariable int userId,
            @RequestBody Map<String, String> updateData) {

        try {
            User updatedUser = userService.updateUserInfo(
                    userId,
                    updateData.get("username"),
                    updateData.get("fullName"),
                    updateData.get("phone")
            );
            updatedUser.setPassword(null);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 更新密码
    @PutMapping("/{userId}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable int userId,
            @RequestBody Map<String, String> passwordData) {

        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("旧密码和新密码不能为空");
        }

        try {
            boolean result = userService.updatePassword(userId, oldPassword, newPassword);
            if (result) {
                return ResponseEntity.ok("密码更新成功");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("旧密码不正确");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}