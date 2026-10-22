package dev.mmatokovic.pantrio.infra.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class OpenAPIConfig {

    private final String appName;
    private final String appVersion;

    OpenAPIConfig(@Value("${spring.application.name}") String appName,
                  @Value("${info.app.version}") String appVersion) {
        this.appName = appName;
        this.appVersion = appVersion;
    }

    @Bean
    OpenAPI defineOpenAPI() {
        return new OpenAPI()
            .components(new Components().addSecuritySchemes("basicScheme", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
            .info(new Info().title(appName).version(appVersion));
    }
}
