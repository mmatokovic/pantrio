package dev.mmatokovic.pantrio.infra.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "info.app")
public record AppInfoProperties(
    @NotBlank String name,
    @NotBlank String version
) { }
