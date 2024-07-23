package tuneandmanner.wiselydiarybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WiselyDiaryBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WiselyDiaryBackendApplication.class, args);
    }

}
