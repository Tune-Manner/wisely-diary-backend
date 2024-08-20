package tuneandmanner.wiselydiarybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tuneandmanner.wiselydiarybackend.common.config.AwsProperties;
import tuneandmanner.wiselydiarybackend.common.config.SupabaseProperties;

@SpringBootApplication
@EnableFeignClients
@EnableConfigurationProperties({AwsProperties.class, SupabaseProperties.class})
public class WiselyDiaryBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(WiselyDiaryBackendApplication.class, args);
    }
}