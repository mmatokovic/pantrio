package dev.mmatokovic.pantrio.infra.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record SecurityUsersProperties(
    Credentials user,
    Credentials prometheus
) {

    public record Credentials(
        String password
    ) { }
}
