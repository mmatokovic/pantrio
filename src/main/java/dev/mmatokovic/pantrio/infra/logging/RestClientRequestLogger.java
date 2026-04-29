package dev.mmatokovic.pantrio.infra.logging;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static dev.mmatokovic.pantrio.infra.logging.HttpLoggingUtils.MAX_BODY_BYTES;

@Slf4j(topic = "audit.logger")
@Component
public final class RestClientRequestLogger implements ClientHttpRequestInterceptor {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final List<String> IGNORED_PREFIXES = List.of("/actuator");

    @Override
    public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request,
                                                 byte @NonNull [] body,
                                                 @NonNull ClientHttpRequestExecution execution)
        throws IOException {

        final var uri = request.getURI().toString();
        final var path = request.getURI().getPath();
        if (IGNORED_PREFIXES.stream().anyMatch(path::startsWith)) {
            return execution.execute(request, body);
        }

        final var ctx = new LogContext(UUID.randomUUID().toString(), request.getMethod().name(), uri, request.getURI().getHost(), Instant.now());
        request.getHeaders().set(REQUEST_ID_HEADER, ctx.requestId());

        logRequest(ctx, request.getHeaders().getContentType(), body);

        final var response = execution.execute(request, body);
        final var contentType = response.getHeaders().getContentType();
        final var contentLength = response.getHeaders().getContentLength();

        if (HttpLoggingUtils.isBinaryContent(contentType) || contentLength > MAX_BODY_BYTES) {
            logResponse(ctx, response.getStatusCode().value(), contentType, new byte[0], contentLength);
            return response;
        }

        final var responseBody = response.getBody().readNBytes(MAX_BODY_BYTES);
        logResponse(ctx, response.getStatusCode().value(), contentType, responseBody, responseBody.length);

        return new BufferingClientHttpResponseWrapper(response, responseBody);
    }

    private static void logRequest(LogContext ctx, MediaType contentType, byte[] body) {
        log.atInfo()
            .addKeyValue("event.action", "request")
            .addKeyValue("http.request.method", ctx.method())
            .addKeyValue("url.full", ctx.uri())
            .addKeyValue("server.address", ctx.source())
            .addKeyValue("http.request.body.bytes", body.length)
            .addKeyValue("http.request.body.content", HttpLoggingUtils.extractLoggableBody(contentType, body))
            .log(">>> EXTERNAL {} {}", ctx.method(), ctx.uri());
    }

    private static void logResponse(LogContext ctx, int status,
                                    MediaType contentType, byte[] body, long responseBytes) {
        log.atInfo()
            .addKeyValue("event.action", "response")
            .addKeyValue("http.request.method", ctx.method())
            .addKeyValue("url.full", ctx.uri())
            .addKeyValue("server.address", ctx.source())
            .addKeyValue("http.response.status_code", status)
            .addKeyValue("event.duration", ctx.elapsedMs())
            .addKeyValue("http.response.body.bytes", responseBytes)
            .addKeyValue("http.response.body.content", HttpLoggingUtils.extractLoggableBody(contentType, body))
            .log("<<< EXTERNAL {} {} {}", ctx.method(), ctx.uri(), status);
    }

    private record BufferingClientHttpResponseWrapper(
        ClientHttpResponse response,
        byte[] body
    ) implements ClientHttpResponse {

        @Override
        public @NonNull InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public @NonNull HttpStatusCode getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public @NonNull String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public @NonNull HttpHeaders getHeaders() {
            return response.getHeaders();
        }

        @Override
        public void close() {
            response.close();
        }
    }
}
