package com.hsrOptimiser.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class Config {
    @Value("${asagi.url}")
    String asagiUrl;

    @Bean
    public WebClient asagiWebClient() {
        return WebClient.builder()
            .codecs(configurer ->
                configurer.defaultCodecs()
                    .maxInMemorySize(16 * 1024 * 1024))
            .baseUrl(asagiUrl)
            .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure JSON serializer
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(
            Object.class);
        template.setDefaultSerializer(serializer);

        return template;
    }
}
