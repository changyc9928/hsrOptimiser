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

    @Value("${asagi.url:https://honkai.asagi-game.com}")
    String asagiUrl;

    @Value("${inventory.url:http://localhost:8080}")
    String inventoryUrl;

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
    public WebClient inventoryWebClient() {
        return WebClient.builder()
            .codecs(configurer ->
                configurer.defaultCodecs()
                    .maxInMemorySize(16 * 1024 * 1024))
            .baseUrl(inventoryUrl)
            .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(
            Object.class);
        template.setDefaultSerializer(serializer);

        return template;
    }
}
