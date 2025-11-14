package dev.mmatokovic.pantrio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.OracleContainer;

@SpringBootTest // start the app in-memory
@AutoConfigureMockMvc // MockMvc can send emulated HTTP requests without starting Tomcat
@Import(IntegrationTest.TestcontainersConfig.class) // test listeners
public class IntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @TestConfiguration
    public static class TestcontainersConfig {

        @Bean
        @ServiceConnection
        OracleContainer oracle(){
            return new OracleContainer("gvenzl/oracle-xe:21-slim")
                .withUsername("PANTRIO_TEST")
                .withReuse(true);
        }
    }
}
