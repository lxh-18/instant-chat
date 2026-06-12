package com.instantchat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.instantchat.dto.ApiResponse;
import com.instantchat.entity.User;
import com.instantchat.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserMapper userMapper;

    @GetMapping("/search")
    public ApiResponse<List<User>> searchUsers(@RequestParam String keyword,
                                                Authentication authentication) {
        Long currentUserId = (Long) authentication.getPrincipal();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(User::getUsername, keyword)
                .or()
                .like(User::getNickname, keyword)
                .ne(User::getId, currentUserId)
                .last("LIMIT 20");
        List<User> users = userMapper.selectList(wrapper);
        users.forEach(u -> u.setPassword(null));
        return ApiResponse.success(users);
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return ApiResponse.success(user);
    }
}