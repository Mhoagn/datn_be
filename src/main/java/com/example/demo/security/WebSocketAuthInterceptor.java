package com.example.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // Chỉ xác thực lúc CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessagingException("Thiếu token");
            }

            String token = authHeader.substring(7);

            try {
                // Dùng parseAccess() của JwtService để validate
                Jws<Claims> claims = jwtService.parseAccess(token);

                // Lấy userId từ subject
                String userId = claims.getPayload().getSubject();

                // Gắn userId vào session
                accessor.setUser(() -> userId);
                
                System.out.println("[WebSocket Auth] User connected with ID: " + userId);

            } catch (JwtException e) {
                throw new MessagingException("Token không hợp lệ: " + e.getMessage());
            }
        }

        return message;
    }
}
