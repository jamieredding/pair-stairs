package dev.coldhands.pair.stairs.backend.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("dev.coldhands.pair.stairs.backend")
@EntityScan("dev.coldhands.pair.stairs.backend")
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.main(args);
    }
}
