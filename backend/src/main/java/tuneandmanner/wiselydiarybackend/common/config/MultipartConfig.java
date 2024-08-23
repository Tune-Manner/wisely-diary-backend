package tuneandmanner.wiselydiarybackend.common.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartConfig {

    @Autowired
    private FileUploadProperties fileUploadProperties;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofBytes(fileUploadProperties.getMaxFileSize()));
        factory.setMaxRequestSize(DataSize.ofBytes(fileUploadProperties.getMaxRequestSize()));
        return factory.createMultipartConfig();
    }
}