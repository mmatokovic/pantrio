package dev.mmatokovic.pantrio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PantrioApplicationTest {

    @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }
}
