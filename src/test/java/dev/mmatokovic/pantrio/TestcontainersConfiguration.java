package dev.mmatokovic.pantrio;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.OracleContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    OracleContainer oracle(){
        return new OracleContainer("gvenzl/oracle-xe:21-slim")
            .withUsername("PANTRIO_TEST")
            .withReuse(true);
    }
}
