package com.yupi.springbootinit.config;

import com.yupi.springbootinit.manager.AiManager;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ai.manager")
@Data
@Slf4j
public class AiManagerConfig {
    /**
     * modelName
     */
    private String modelName;

    @Bean
    public AiManager aiManager() {
        return new AiManager(modelName);
    }
}
