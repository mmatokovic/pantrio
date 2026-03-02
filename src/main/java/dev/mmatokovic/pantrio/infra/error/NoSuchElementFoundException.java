package dev.mmatokovic.pantrio.infra.error;

public class NoSuchElementFoundException extends RuntimeException {

    public NoSuchElementFoundException(String message) {
        super(message);
    }
}
