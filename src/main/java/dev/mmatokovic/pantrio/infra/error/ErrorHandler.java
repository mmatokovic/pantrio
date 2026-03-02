package dev.mmatokovic.pantrio.infra.error;

import org.apache.commons.lang3.exception.ExceptionUtils;
import jakarta.validation.ConstraintViolationException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Objects;

@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("error.logger");

    @Value("${pantrio.print.trace:false}")
    private boolean printStackTrace;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {

        if (isTraceOn(request)) { LOGGER.error(ex.getMessage(), ex); }

        ErrorResponse body = buildErrorResponse(ex, "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR, request);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        return ResponseEntity.badRequest()
            .body(buildErrorResponse(ex, "Validation failed", HttpStatus.BAD_REQUEST, request));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {
        if (isTraceOn(request)) { LOGGER.error(ex.getMessage(), ex); }

        return ResponseEntity.unprocessableContent()
            .body(buildErrorResponse(ex, "Data integrity violation", HttpStatus.UNPROCESSABLE_CONTENT, request));
    }

    @ExceptionHandler(NoSuchElementFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(NoSuchElementFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(buildErrorResponse(ex, "Product not found", HttpStatus.NOT_FOUND, request));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {

        ErrorResponse errorResponse = buildErrorResponse(ex, "Data integrity violation", HttpStatus.UNPROCESSABLE_CONTENT, request);

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorResponse = errorResponse.addValidationError(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.unprocessableContent().body(errorResponse);
    }

    private static boolean isTraceOn(WebRequest request) {
        String[] value = request.getParameterValues("trace");
        return Objects.nonNull(value) && value.length > 0 && value[0].contentEquals("true");
    }

    private ErrorResponse buildErrorResponse(Exception ex, String message, HttpStatusCode status, WebRequest request) {

        ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), status.value(), message);

        if (printStackTrace && isTraceOn(request)) {
            errorResponse = errorResponse.withTrace(
                ExceptionUtils.getStackTrace(ex)
            );
        }

        return errorResponse;
    }

    private ErrorResponse buildErrorResponse(Exception ex, HttpStatusCode status, WebRequest request) {
        return buildErrorResponse(ex, ex.getMessage(), status, request);
    }

    @Override
    public ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex, Object body, @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        final var errorResponse = buildErrorResponse(ex, status, request);

        return super.handleExceptionInternal(ex, errorResponse, headers, status, request);
    }
}
