package dev.mmatokovic.pantrio.domain.openfoodfacts;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openfoodfacts")
public record OpenFoodFactsProperties(
    String host
) { }
