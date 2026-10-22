package dev.mmatokovic.pantrio.infra.exception;

public class JobManagementException extends RuntimeException {

    public JobManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
