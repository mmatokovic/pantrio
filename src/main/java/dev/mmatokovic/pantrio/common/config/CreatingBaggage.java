package dev.mmatokovic.pantrio.common.config;

import io.micrometer.tracing.Tracer;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class CreatingBaggage implements ResponseBodyAdvice<Object> {

    private final Tracer tracer;

    public CreatingBaggage(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        if (tracer.currentSpan() != null) {
            String traceId = tracer.currentSpan().context().traceId();
            response.getHeaders().add("trace-id", traceId);
        }

        return body;
    }
}
