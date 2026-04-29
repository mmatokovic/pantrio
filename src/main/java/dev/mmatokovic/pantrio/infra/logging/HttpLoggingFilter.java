package dev.mmatokovic.pantrio.infra.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static dev.mmatokovic.pantrio.infra.logging.HttpLoggingUtils.MAX_BODY_BYTES;

@Slf4j(topic = "audit.logger")
@Component
final class HttpLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER_KEY = "X-Request-Id";
    private static final String LOAD_BALANCER_HEADER_KEY = "X-Forwarded-For";
    private static final List<String> IGNORED_APIS = List.of(
        "/v3/api-docs",
        "/swagger-ui",
        "/actuator"
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
        throws ServletException, IOException {

        if (isIgnored(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        final var req = new ContentCachingRequestWrapper(request, MAX_BODY_BYTES);
        final var res = new ContentCachingResponseWrapper(response);
        final var ctx = buildContext(req);

        MDC.put("requestId", ctx.requestId());
        response.addHeader(REQUEST_ID_HEADER_KEY, ctx.requestId());

        try {
            chain.doFilter(req, res);
        } finally {
            logRequest(ctx, req);
            logResponse(ctx, res);
            res.copyBodyToResponse();
            MDC.clear();
        }
    }

    private static void logRequest(LogContext ctx, ContentCachingRequestWrapper req) {
        var body = req.getContentAsByteArray();
        log.atInfo()
            .addKeyValue("event.action", "request")
            .addKeyValue("http.request.method", ctx.method())
            .addKeyValue("url.path", ctx.uri())
            .addKeyValue("client.ip", ctx.source())
            .addKeyValue("http.request.body.bytes", body.length)
            .addKeyValue("http.request.body.content", HttpLoggingUtils.extractLoggableBody(req.getContentType(), body))
            .log(">>> {} {}", ctx.method(), ctx.uri());
    }

    private static void logResponse(LogContext ctx, ContentCachingResponseWrapper res) {
        var body = res.getContentAsByteArray();
        log.atInfo()
            .addKeyValue("event.action", "response")
            .addKeyValue("http.request.method", ctx.method())
            .addKeyValue("url.path", ctx.uri())
            .addKeyValue("http.response.status_code", res.getStatus())
            .addKeyValue("event.duration", ctx.elapsedMs())
            .addKeyValue("http.response.body.bytes", body.length)
            .addKeyValue("http.response.body.content", HttpLoggingUtils.extractLoggableBody(res.getContentType(), body))
            .log("<<< {} {} {}", ctx.method(), ctx.uri(), res.getStatus());
    }

    private static LogContext buildContext(HttpServletRequest req) {
        return new LogContext(
            resolveRequestId(req),
            req.getMethod(),
            buildUri(req),
            resolveSource(req),
            Instant.now()
        );
    }

    private static boolean isIgnored(String uri) {
        return IGNORED_APIS.stream().anyMatch(uri::startsWith);
    }

    private static String resolveRequestId(HttpServletRequest req) {
        var fromHeader = req.getHeader(REQUEST_ID_HEADER_KEY);
        return StringUtils.isNotBlank(fromHeader) ? fromHeader : UUID.randomUUID().toString();
    }

    private static String resolveSource(HttpServletRequest req) {
        var forwarded = req.getHeader(LOAD_BALANCER_HEADER_KEY);
        return StringUtils.isNotBlank(forwarded) ? forwarded : req.getRemoteAddr();
    }

    private static String buildUri(HttpServletRequest req) {
        var builder = UriComponentsBuilder.fromPath(req.getRequestURI());
        req.getParameterMap().forEach((k, v) -> builder.queryParam(k, (Object[]) v));
        return builder.toUriString();
    }
}
