package club.ss220.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "club.ss220")
@EnableScheduling
public class SS220ManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SS220ManagerApplication.class, args);
    }
}
