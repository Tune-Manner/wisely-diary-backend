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
public class OpenAiConfig {
    private String apiKey;
    private String chatModel;
    private String imageSize;
    private String imageModel;
    private String imageQuality;
    private String imageStyle;

    @PostConstruct
    public void logConfig() {
        if (apiKey != null && !apiKey.isEmpty()) {
            log.info("OpenAI API Key: {}", apiKey.substring(0, Math.min(apiKey.length(), 5)) + "...");
        } else {
            log.warn("OpenAI API Key is not set or empty");
        }
        log.info("ChatGPT Model: {}", chatModel);
        log.info("DALL-E Image Model: {}", imageModel);
        log.info("DALL-E Image Size: {}", imageSize);
        log.info("DALL-E Image Quality: {}", imageQuality);
        log.info("DALL-E Image Style: {}", imageStyle);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}