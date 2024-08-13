package tuneandmanner.wiselydiarybackend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/cartoonImage/**")
                .addResourceLocations("file:C:/00_ILGI/backend/backend/src/main/resources/cartoonImage/");
    }
}