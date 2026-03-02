package dev.mmatokovic.pantrio;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.wiremock.spring.EnableWireMock;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import({
    BaseIntegrationTest.TestContainersConfig.class,
})
@EnableWireMock
public abstract class BaseIntegrationTest {

    @TestConfiguration(proxyBeanMethods = false)
    public static class TestContainersConfig {

        private static final OracleContainer DATABASE_CONTAINER =
            new OracleContainer(DockerImageName.parse("gvenzl/oracle-xe:21-slim-faststart"))
                .withUsername("PANTRIO_TEST")
                .withReuse(true);

        @Bean
        @ServiceConnection
        OracleContainer databaseContainer(){
            return DATABASE_CONTAINER;
        }
    }
}
