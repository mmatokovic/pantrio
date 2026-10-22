package dev.mmatokovic.pantrio.infra.job;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "batch.job")
public record BatchJobProperties(
    boolean enabled,
    String schedule
) { }
