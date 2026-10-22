package dev.mmatokovic.pantrio.infra.openfoodfacts;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openfoodfacts")
public record OpenFoodFactsProperties(
    String host
) { }
