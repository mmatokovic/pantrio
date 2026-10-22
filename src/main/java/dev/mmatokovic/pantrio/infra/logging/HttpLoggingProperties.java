package dev.mmatokovic.pantrio.infra.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "pantrio.logging.http")
public record HttpLoggingProperties(
    int maxBodyBytes,
    List<String> excludedPaths,
    Set<String> excludedHeaders
) {

    static final Set<String> DEFAULT_EXCLUDED_HEADERS = Set.of(
        "authorization",
        "proxy-authorization",
        "cookie",
        "set-cookie"
    );

    static final List<String> DEFAULT_EXCLUDED_PATHS = List.of(
        "actuator",
        "swagger-ui",
        "v3/api-docs"
    );

    public HttpLoggingProperties {
        if (maxBodyBytes <= 0) {
            maxBodyBytes = 8 * 1024;
        }
        if (excludedPaths == null) {
            excludedPaths = DEFAULT_EXCLUDED_PATHS;
        }
        excludedHeaders = (excludedHeaders == null || excludedHeaders.isEmpty())
            ? DEFAULT_EXCLUDED_HEADERS
            : excludedHeaders.stream().map(header -> header.toLowerCase(Locale.ROOT)).collect(Collectors.toUnmodifiableSet());
    }
}
