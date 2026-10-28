package dev.mmatokovic.pantrio.infra.openfoodfacts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "openfoodfacts")
public record OpenFoodFactsProperties(
    @NotBlank String host,
    @NotNull Duration connectTimeout,
    @NotNull Duration readTimeout
) { }
