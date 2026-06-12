package com.instantchat.websocket;

import com.instantchat.entity.User;
import com.instantchat.mapper.UserMapper;
import com.instantchat.utils.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class WebSocketAuthHelper {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    public Long authenticate(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenProvider.validateToken(token)) {
                return jwtTokenProvider.getUserIdFromToken(token);
            }
        }
        return null;
    }
}