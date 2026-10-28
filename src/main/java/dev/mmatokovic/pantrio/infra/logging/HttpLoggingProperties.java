package dev.mmatokovic.pantrio.infra.logging;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@ConfigurationProperties(prefix = "pantrio.logging.http")
public record HttpLoggingProperties(
    @DefaultValue("8192") @Positive int maxBodyBytes,
    @DefaultValue({"actuator", "swagger-ui", "v3/api-docs"}) List<String> excludedPaths,
    @DefaultValue({"authorization", "proxy-authorization", "cookie", "set-cookie"}) Set<String> excludedHeaders
) {

    public HttpLoggingProperties {
        excludedHeaders = excludedHeaders.stream().map(header -> header.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }
}
