package dev.mmatokovic.pantrio.infra.telemetry;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class TraceparentResponseFilter extends OncePerRequestFilter {

    private final W3CTraceContextPropagator propagator = W3CTraceContextPropagator.getInstance();

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain chain
    ) throws ServletException, IOException {

        SpanContext spanContext = Span.current().getSpanContext();
        if (spanContext.isValid()) {
            propagator.inject(Context.current(), response, HttpServletResponse::addHeader);
            propagator.fields().forEach(field -> {
                String value = response.getHeader(field);
                if (value != null) {
                    MDC.put(field, value);
                }
            });
        }

        chain.doFilter(request, response);
    }
}
