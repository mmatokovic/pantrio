package dev.mmatokovic.pantrio.infra.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

@Slf4j
@RequiredArgsConstructor
public final class RestClientRequestLogger implements ClientHttpRequestInterceptor {

    private static final int RESPONSE_CACHE_LIMIT = 1024 * 1024;

    private final HttpLoggingProperties properties;

    @Override
    public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request,
                                                 byte @NonNull [] body,
                                                 @NonNull ClientHttpRequestExecution execution) throws IOException {
        var startNanos = System.nanoTime();
        logRequest(request, body);

        var response = execution.execute(request, body);
        var contentType = response.getHeaders().getContentType();
        var contentLength = response.getHeaders().getContentLength();
        if (HttpLoggingUtils.isBinaryContent(contentType) || contentLength > RESPONSE_CACHE_LIMIT) {
            logResponse(request, response.getStatusCode(), contentType, new byte[0], System.nanoTime() - startNanos);
            return response;
        }

        var bodyStream = response.getBody();
        var bufferedBody = bodyStream.readNBytes(RESPONSE_CACHE_LIMIT + 1);
        if (bufferedBody.length > RESPONSE_CACHE_LIMIT) {
            logResponse(request, response.getStatusCode(), contentType, new byte[0], System.nanoTime() - startNanos);
            return new BufferedClientHttpResponse(response, new SequenceInputStream(new ByteArrayInputStream(bufferedBody), bodyStream));
        }
        logResponse(request, response.getStatusCode(), contentType, bufferedBody, System.nanoTime() - startNanos);
        return new BufferedClientHttpResponse(response, new ByteArrayInputStream(bufferedBody));
    }

    private void logRequest(HttpRequest request, byte[] body) {
        var contentType = request.getHeaders().getContentType();
        log.atInfo()
            .addKeyValue("bytes", body.length)
            .addKeyValue("content", readableBody(HttpLoggingUtils.isLoggableContent(contentType), body, contentType))
            .log("HTTP {} outbound request :: {}", request.getMethod(), request.getURI());
    }

    private void logResponse(HttpRequest request, HttpStatusCode status, @Nullable MediaType contentType, byte[] body, long durationNanos) {
        var logBody = status.isError() || HttpLoggingUtils.isLoggableContent(contentType);
        var event = status.is5xxServerError() ? log.atError() : status.isError() ? log.atWarn() : log.atInfo();
        event
            .addKeyValue("bytes", body.length)
            .addKeyValue("content", readableBody(logBody, body, contentType))
            .log("HTTP {} outbound response :: {} {} in {} ms", request.getMethod(), request.getURI(), status.value(), durationNanos / 1_000_000);
    }

    private @Nullable String readableBody(boolean shouldLog, byte[] body, @Nullable MediaType contentType) {
        if (!shouldLog || body.length == 0) {
            return null;
        }
        return HttpLoggingUtils.normalizeBody(body, contentType == null ? null : contentType.toString(), properties.maxBodyBytes());
    }

    @RequiredArgsConstructor
    private static final class BufferedClientHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse response;
        private final InputStream body;

        @Override
        public @NonNull InputStream getBody() {
            return body;
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
