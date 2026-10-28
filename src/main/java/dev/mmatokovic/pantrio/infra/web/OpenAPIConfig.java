package dev.mmatokovic.pantrio.infra.web;

import dev.mmatokovic.pantrio.infra.config.AppInfoProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class OpenAPIConfig {

    @Bean
    OpenAPI defineOpenAPI(AppInfoProperties appInfoProperties) {
        return new OpenAPI()
            .components(new Components().addSecuritySchemes("basicScheme", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
            .info(new Info().title(appInfoProperties.name()).version(appInfoProperties.version()));
    }
}
