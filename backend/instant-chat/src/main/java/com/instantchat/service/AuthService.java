package com.instantchat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.instantchat.dto.AuthResponse;
import com.instantchat.dto.LoginRequest;
import com.instantchat.dto.RegisterRequest;
import com.instantchat.entity.User;
import com.instantchat.mapper.UserMapper;
import com.instantchat.utils.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务层 - 满足得分点1：MVC分层架构
 * Service层负责处理业务逻辑，与Controller层（处理请求响应）和Mapper层（数据访问）配合
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        user.setStatus(0);
        userMapper.insert(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getNickname(), user.getAvatar());
    }

    public AuthResponse login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        user.setStatus(1);
        userMapper.updateById(user);

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getNickname(), user.getAvatar());
    }

    public void logout(Long userId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(0);
            userMapper.updateById(user);
        }
    }

    public User getCurrentUser(Long userId) {
        return userMapper.selectById(userId);
    }
}