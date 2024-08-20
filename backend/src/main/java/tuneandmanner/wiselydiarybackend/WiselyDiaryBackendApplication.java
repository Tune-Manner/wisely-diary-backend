package tuneandmanner.wiselydiarybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WiselyDiaryBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(WiselyDiaryBackendApplication.class, args);
    }
}
