package com.hsrOptimiser.controller;

import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    public static final String TOKEN_KEY_PREFIX = "auth:token:";

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/auth/token")
    public ResponseEntity<Map<String, Object>> issueToken() {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + token, token, java.time.Duration.ofHours(24));
        return ResponseEntity.status(HttpStatus.OK)
            .body(Map.of("token", token, "expiresIn", "24h"));
    }
}
