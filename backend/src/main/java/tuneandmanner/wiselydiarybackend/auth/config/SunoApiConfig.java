package tuneandmanner.wiselydiarybackend.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "suno.api")
public class SunoApiConfig {

    private String token;

}
