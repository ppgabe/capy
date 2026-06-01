package dev.ailuruslabs.capybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CapyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CapyBackendApplication.class, args);
    }

}
