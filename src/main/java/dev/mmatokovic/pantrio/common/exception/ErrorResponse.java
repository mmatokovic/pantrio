package dev.mmatokovic.pantrio.common.exception;

public class ErrorResponse {

    String message;
    int code;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public ErrorResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
