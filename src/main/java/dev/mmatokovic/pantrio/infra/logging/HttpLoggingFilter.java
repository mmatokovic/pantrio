package dev.mmatokovic.pantrio.infra.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
final class HttpLoggingFilter extends OncePerRequestFilter {

    static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final int REQUEST_CACHE_LIMIT = 1024 * 1024;
    private static final String MDC_REQUEST_ID = "request.id";
    private static final Pattern REQUEST_ID_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    private final HttpLoggingProperties properties;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        var path = request.getRequestURI();
        return properties.excludedPaths().stream().anyMatch(path::contains);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        var requestWrapper = new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT);
        var responseWrapper = new ContentCachingResponseWrapper(response);
        var startNanos = System.nanoTime();
        var requestId = resolveRequestId(request);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        MDC.put(MDC_REQUEST_ID, requestId);
        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            logExchange(requestWrapper, responseWrapper, System.nanoTime() - startNanos);
            responseWrapper.copyBodyToResponse();
            MDC.remove(MDC_REQUEST_ID);
        }
    }

    private static String resolveRequestId(HttpServletRequest request) {
        var requestId = request.getHeader(REQUEST_ID_HEADER);
        return requestId != null && REQUEST_ID_PATTERN.matcher(requestId).matches() ? requestId : UUID.randomUUID().toString();
    }

    private void logExchange(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long durationNanos) {
        try {
            logRequest(request);
            logResponse(request, response, durationNanos);
        } catch (RuntimeException e) {
            log.warn("HTTP logging failed", e);
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        var body = request.getContentAsByteArray();
        var logBody = HttpLoggingUtils.shouldLogRequestBody(request.getMethod(), request.getContentType());
        log.atInfo()
            .addKeyValue("query", request.getQueryString())
            .addKeyValue("source", request.getRemoteAddr())
            .addKeyValue("headers", collectLoggableHeaders(Collections.list(request.getHeaderNames()), name -> Collections.list(request.getHeaders(name))))
            .addKeyValue("bytes", body.length)
            .addKeyValue("truncated", body.length == REQUEST_CACHE_LIMIT)
            .addKeyValue("content", readableBody(logBody, body, request.getContentType()))
            .log("HTTP {} request :: {}", request.getMethod(), request.getRequestURI());
    }

    private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long durationNanos) {
        var status = response.getStatus();
        var body = response.getContentAsByteArray();
        var logBody = status >= 400 || HttpLoggingUtils.shouldLogResponseBody(response.getContentType());
        var event = status >= 500 ? log.atError() : status >= 400 ? log.atWarn() : log.atInfo();
        event
            .addKeyValue("headers", collectLoggableHeaders(response.getHeaderNames(), response::getHeaders))
            .addKeyValue("bytes", body.length)
            .addKeyValue("content", readableBody(logBody, body, response.getContentType()))
            .log("HTTP {} response :: {} {} in {} ms", request.getMethod(), request.getRequestURI(), status, durationNanos / 1_000_000);
    }

    private Map<String, String> collectLoggableHeaders(Collection<String> names, HeaderValues values) {
        var headers = new TreeMap<String, String>();
        for (var name : names) {
            if (!properties.excludedHeaders().contains(name.toLowerCase(Locale.ROOT))) {
                headers.put(name, String.join(", ", values.of(name)));
            }
        }
        return headers;
    }

    private @Nullable String readableBody(boolean shouldLog, byte[] body, @Nullable String contentType) {
        if (!shouldLog || body.length == 0) {
            return null;
        }
        return HttpLoggingUtils.normalizeBody(body, contentType, properties.maxBodyBytes());
    }

    @FunctionalInterface
    private interface HeaderValues {
        Collection<String> of(String name);
    }
}
