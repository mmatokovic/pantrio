package dev.mmatokovic.pantrio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableResilientMethods
public class PantrioApplication {

    static void main(String[] args) {
        SpringApplication.run(PantrioApplication.class, args);
    }
}
