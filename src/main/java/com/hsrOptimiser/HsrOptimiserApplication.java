package com.hsrOptimiser;

import com.hsrOptimiser.properties.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(Properties.class)
public class HsrOptimiserApplication {

    public static void main(String[] args) {
        SpringApplication.run(HsrOptimiserApplication.class, args);
    }

}
