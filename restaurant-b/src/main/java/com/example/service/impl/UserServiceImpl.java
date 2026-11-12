package com.example.service.impl;

import com.example.entity.User;
import com.example.repository.UserRepository;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getUserById(int userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    @Transactional
    public User updateUserInfo(int userId, String username, String fullName, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 检查用户名是否已被使用（排除当前用户）
        if (username != null && !username.equals(user.getUsername())) {
            Optional<User> existingUser = Optional.ofNullable(userRepository.findByUsername(username));
            if (existingUser.isPresent() && existingUser.get().getUserId() != userId) {
                throw new IllegalArgumentException("用户名已被使用");
            }
            user.setUsername(username);
        }

        if (fullName != null) {
            user.setFullName(fullName);
        }

        if (phone != null) {
            user.setPhone(phone);
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public boolean updatePassword(int userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        // 验证旧密码
        String encryptedOldPassword = encryptPassword(oldPassword);
        if (!user.getPassword().equals(encryptedOldPassword)) {
            return false;
        }

        // 更新密码
        String encryptedNewPassword = encryptPassword(newPassword);
        user.setPassword(encryptedNewPassword);
        userRepository.save(user);
        return true;
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