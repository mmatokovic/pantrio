package dev.mmatokovic.pantrio;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.spring.EnableWireMock;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
    "security.user.password=" + BaseIntegrationTest.USER_PWD,
    "security.prometheus.password=prometheus"
})
@Import(BaseIntegrationTest.TestContainersConfig.class)
@EnableWireMock
public abstract class BaseIntegrationTest {

    protected static final String USER_NAME = "user";
    protected static final String USER_PWD = "userpwd";

    protected JdbcTemplate jdbcTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.basePath = "";
        jdbcTemplate = SeedJdbcTemplateHolder.JDBC_TEMPLATE;
    }

    private static final class SeedJdbcTemplateHolder {

        private static final JdbcTemplate JDBC_TEMPLATE = createSeedJdbcTemplate();

        private static JdbcTemplate createSeedJdbcTemplate() {
            var container = TestContainersConfig.DATABASE_CONTAINER;
            var seedDataSource = new SingleConnectionDataSource(container.getJdbcUrl(), container.getUsername(), container.getPassword(), true);
            seedDataSource.setAutoCommit(true);
            return new JdbcTemplate(seedDataSource);
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestContainersConfig {

        private static final OracleContainer DATABASE_CONTAINER =
            new OracleContainer(DockerImageName.parse("gvenzl/oracle-free:23-slim-faststart"))
                .withUsername("PANTRIO_TEST")
                .withReuse(true);

        @Bean
        @ServiceConnection
        OracleContainer databaseContainer() {
            return DATABASE_CONTAINER;
        }
    }
}
