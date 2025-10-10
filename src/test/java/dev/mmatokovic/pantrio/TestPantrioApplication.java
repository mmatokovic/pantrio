package dev.mmatokovic.pantrio;

import org.springframework.boot.SpringApplication;

public class TestPantrioApplication {

    public static void main(String[] args) {
        SpringApplication.from(PantrioApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
