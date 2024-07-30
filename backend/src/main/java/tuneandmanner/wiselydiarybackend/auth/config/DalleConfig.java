package tuneandmanner.wiselydiarybackend.auth.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.ai.openai")
public class DalleConfig {
    private String apiKey;

    @PostConstruct
    public void logApiKey() {
        if (apiKey != null && !apiKey.isEmpty()) {
            log.info("DALL-E API Key: {}", apiKey.substring(0, Math.min(apiKey.length(), 5)) + "...");
        } else {
            log.warn("DALL-E API Key is not set or empty");
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}