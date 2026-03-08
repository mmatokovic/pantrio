package dev.mmatokovic.pantrio.infra.security;

import dev.mmatokovic.pantrio.infra.error.ErrorResponse;
import dev.mmatokovic.pantrio.infra.jackson.ObjectMapperUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(@NonNull HttpServletRequest request,
                         @NonNull HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = new ErrorResponse(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Authentication failed",
            null);

        String json = ObjectMapperUtils.writeJson(errorResponse);
        response.getWriter().write(json);
    }

}
