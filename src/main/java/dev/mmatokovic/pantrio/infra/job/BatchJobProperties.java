package dev.mmatokovic.pantrio.infra.job;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "batch.job")
public record BatchJobProperties(
    @NotBlank String schedule
) { }
