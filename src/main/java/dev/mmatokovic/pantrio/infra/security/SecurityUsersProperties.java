package dev.mmatokovic.pantrio.infra.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "security")
public record SecurityUsersProperties(
    @Valid @NotNull Credentials user,
    @Valid @NotNull Credentials prometheus
) {

    public record Credentials(
        @NotBlank String password
    ) { }
}
