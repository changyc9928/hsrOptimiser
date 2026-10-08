package com.hsrOptimiser.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EVALUATE_QUEUE = "evaluate.jobs";

    @Bean
    public Queue evaluateJobsQueue() {
        return new Queue(EVALUATE_QUEUE, true);
    }
}
