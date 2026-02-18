package org.lucas.arbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "org.lucas.arbackend.repository")
public class ArBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArBackendApplication.class, args);
    }

}
