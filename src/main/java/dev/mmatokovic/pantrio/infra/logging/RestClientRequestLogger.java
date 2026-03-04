package dev.mmatokovic.pantrio.infra.logging;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.mmatokovic.pantrio.infra.logging.HttpLoggingUtils.LOG_ITEM_DELIMITER;

@Slf4j(topic = "audit.logger")
@Component
public final class RestClientRequestLogger implements ClientHttpRequestInterceptor {

    private static final List<String> IGNORABLE_URIS = new ArrayList<>();

    static {
        IGNORABLE_URIS.add("/actuator");
    }

    @Override
    public @NonNull ClientHttpResponse intercept(HttpRequest request, byte @NonNull [] body, @NonNull ClientHttpRequestExecution execution)
        throws IOException {
        final var httpMethod = request.getMethod();
        final var requestUri = request.getURI().toString();
        final var requestId = UUID.randomUUID().toString();
        final var startTime = Instant.now();

        String traceparent = MDC.get("traceparent");
        request.getHeaders().set("traceparent", traceparent);

        final var shouldIgnoreLogging = IGNORABLE_URIS.stream().anyMatch(requestUri::contains);
        if (shouldIgnoreLogging) {
            return execution.execute(request, body);
        }

        logRequest(body, requestId, requestUri, httpMethod, request.getHeaders());

        final ClientHttpResponse response = execution.execute(request, body);

        final byte[] responseBody = response.getBody().readAllBytes();
        logResponse(response.getStatusCode().value(), responseBody, response.getHeaders().getContentType(), requestId, requestUri, httpMethod,
            startTime);

        return new BufferingClientHttpResponseWrapper(response, responseBody);
    }

    private static void logRequest(byte[] requestContent, String requestId, String requestUri, HttpMethod httpMethod, HttpHeaders headers) {
        final var requestSb = new StringBuilder();
        requestSb
            .append("EXTERNAL HTTP ").append(httpMethod.name()).append(" request :: ")
            .append("Request ID: ").append(requestId).append(LOG_ITEM_DELIMITER)
            .append("Headers: ").append(headers).append(LOG_ITEM_DELIMITER)
            .append("Request URI: ").append(requestUri);

        if (HttpLoggingUtils.shouldLogRequestBody(httpMethod.name(), headers.getContentType())) {
            if (ArrayUtils.isEmpty(requestContent)) {
                requestSb.append(LOG_ITEM_DELIMITER).append("Request body is empty");
            } else {
                requestSb.append(LOG_ITEM_DELIMITER).append("Request body: ").append(HttpLoggingUtils.normalizeBody(requestContent));
            }
        }

        log.info(requestSb.toString());
    }

    private static void logResponse(int responseStatus,
                                    byte[] responseContent,
                                    MediaType contentType,
                                    String requestId,
                                    String requestUri,
                                    HttpMethod httpMethod,
                                    Instant startTime) {
        final var duration = Duration.between(startTime, Instant.now()).toMillis();
        final var responseSb = new StringBuilder();
        responseSb
            .append("EXTERNAL HTTP ").append(httpMethod.name()).append(" response :: ")
            .append("Request ID: ").append(requestId).append(LOG_ITEM_DELIMITER)
            .append("Request URI: ").append(requestUri).append(LOG_ITEM_DELIMITER)
            .append("Duration: ").append(duration).append("ms").append(LOG_ITEM_DELIMITER)
            .append("Response status: ").append(responseStatus).append(LOG_ITEM_DELIMITER);

        if (HttpLoggingUtils.shouldLogResponseBody(contentType)) {
            if (ArrayUtils.isEmpty(responseContent)) {
                responseSb.append("Response body is empty");
            } else {
                responseSb.append("Response body: ").append(HttpLoggingUtils.normalizeBody(responseContent));
            }
        }

        log.info(responseSb.toString());
    }

    private record BufferingClientHttpResponseWrapper(ClientHttpResponse response, byte[] body) implements ClientHttpResponse {

        @Override
        public  @NonNull InputStream getBody() {
            return new ByteArrayInputStream(body);
        }

        @Override
        public  @NonNull HttpStatusCode getStatusCode() throws IOException {
            return response.getStatusCode();
        }

        @Override
        public @NonNull String getStatusText() throws IOException {
            return response.getStatusText();
        }

        @Override
        public  void close() {
            response.close();
        }

        @Override
        public @NonNull HttpHeaders getHeaders() {
            return response.getHeaders();
        }

    }
}
