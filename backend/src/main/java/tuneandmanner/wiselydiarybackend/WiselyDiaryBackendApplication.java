package tuneandmanner.wiselydiarybackend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.springframework.core.env.Environment;
import tuneandmanner.wiselydiarybackend.auth.config.OpenAiConfig;
import tuneandmanner.wiselydiarybackend.common.config.AwsProperties;
import tuneandmanner.wiselydiarybackend.common.config.FileUploadProperties;
import tuneandmanner.wiselydiarybackend.common.config.SupabaseProperties;

@Slf4j
@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties({AwsProperties.class, SupabaseProperties.class, OpenAiConfig.class, FileUploadProperties.class})
public class WiselyDiaryBackendApplication {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
            .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
            .setFieldMatchingEnabled(true);
        return modelMapper;
    }
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(WiselyDiaryBackendApplication.class, args);
        Environment env = context.getEnvironment();

        log.info("OpenAI Configuration:");
        log.info("spring.ai.openai.api-key: {}", maskApiKey(env.getProperty("spring.ai.openai.api-key")));
        log.info("spring.ai.openai.chat-model: {}", env.getProperty("spring.ai.openai.chat-model"));
        log.info("spring.ai.openai.image-size: {}", env.getProperty("spring.ai.openai.image-size"));
        log.info("spring.ai.openai.image-model: {}", env.getProperty("spring.ai.openai.image-model"));
        log.info("spring.ai.openai.image-quality: {}", env.getProperty("spring.ai.openai.image-quality"));
        log.info("spring.ai.openai.audio-model: {}", env.getProperty("spring.ai.openai.audio-model"));
        log.info("spring.ai.openai.urls.base-url: {}", env.getProperty("spring.ai.openai.urls.base-url"));
        log.info("spring.ai.openai.urls.create-transcription-url: {}", env.getProperty("spring.ai.openai.urls.create-transcription-url"));
        log.info("app.cartoon.image-path: {}", env.getProperty("app.cartoon.image-path"));

    }

    private static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "Not set or invalid";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}