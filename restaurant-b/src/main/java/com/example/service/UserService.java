package com.example.service;

import com.example.entity.User;

public interface UserService {
    User getUserById(int userId);
    User updateUserInfo(int userId, String username, String fullName, String phone);
    boolean updatePassword(int userId, String oldPassword, String newPassword);
}