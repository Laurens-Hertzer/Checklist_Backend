package ch.bbw.lah.checklist_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import lombok.*;

@SpringBootApplication
@EnableScheduling
public class ChecklistBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChecklistBackendApplication.class, args);
    }

}
