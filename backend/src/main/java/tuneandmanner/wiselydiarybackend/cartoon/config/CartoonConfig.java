package tuneandmanner.wiselydiarybackend.cartoon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "app.cartoon")
@Getter
@Setter
public class CartoonConfig {
    private String imagePath;
}