package tuneandmanner.wiselydiarybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import tuneandmanner.wiselydiarybackend.common.config.AwsProperties;
import tuneandmanner.wiselydiarybackend.common.config.SupabaseProperties;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties({AwsProperties.class, SupabaseProperties.class})
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
        SpringApplication.run(WiselyDiaryBackendApplication.class, args);
    }
}