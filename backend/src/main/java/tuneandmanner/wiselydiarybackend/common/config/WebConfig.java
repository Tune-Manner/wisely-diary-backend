package tuneandmanner.wiselydiarybackend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost",
                        "http://10.0.2.2",
                        "http://3.38.180.88:8080",  // EC2 서버 주소 추가
                        "http://3.38.180.88",       // 포트 없는 버전도 추가
                        "https://3.38.180.88:8080", // HTTPS 버전 추가 (필요한 경우)
                        "https://3.38.180.88"       // HTTPS 포트 없는 버전 추가 (필요한 경우)
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/cartoonImage/**")
                .addResourceLocations("file:C:/00_ILGI/backend/backend/src/main/resources/cartoonImage/");
    }
}