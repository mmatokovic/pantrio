package dev.mmatokovic.pantrio.infra.exception;

public class NoSuchElementFoundException extends RuntimeException {

    public NoSuchElementFoundException(String message) {
        super(message);
    }
}
