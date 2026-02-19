package dev.mmatokovic.pantrio.infra.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenAPIConfig {

    private final BuildProperties buildProperties;

    @Bean
    public OpenAPI defineOpenAPI() {
        return new OpenAPI()
            .components(new Components().addSecuritySchemes(
                "basicScheme",
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")))
            .info(new Info()
                .title(buildProperties.getName())
                .version(buildProperties.getVersion()));
    }
}
