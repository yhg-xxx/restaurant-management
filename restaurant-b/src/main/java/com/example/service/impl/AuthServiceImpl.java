package com.example.service.impl;


import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User authenticate(String username, String password) {
        // 1. 根据用户名查找用户
        User user = userRepository.findByUsername(username);

        if (user == null) {
            return null; // 用户不存在
        }

        // 2. 对输入的密码进行SHA-256加密
        String encryptedPassword = encryptPassword(password);

        // 3. 比较加密后的密码
        if (user.getPassword().equals(encryptedPassword)) {
            return user; // 认证成功
        }

        return null; // 密码不匹配
    }

    // SHA-256加密方法
    private String encryptPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(
                    password.getBytes(StandardCharsets.UTF_8));

            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("加密算法不可用", e);
        }
    }
}