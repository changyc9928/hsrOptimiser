package com.hsrOptimiser.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsrOptimiser.controller.AuthController;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            reject(response, HttpStatus.UNAUTHORIZED, "Missing or malformed Authorization header");
            return false;
        }
        String token = auth.substring("Bearer ".length()).trim();
        Object stored = redisTemplate.opsForValue().get(AuthController.TOKEN_KEY_PREFIX + token);
        if (stored == null || !token.equals(stored.toString())) {
            reject(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            return false;
        }
        request.setAttribute("userId", token);
        return true;
    }

    private void reject(HttpServletResponse response, HttpStatus status, String message) throws Exception {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
            Map.of("success", false, "message", message, "statusCode", status.value())));
    }
}

