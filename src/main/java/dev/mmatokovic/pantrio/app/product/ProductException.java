package dev.mmatokovic.pantrio.app.product;

public class ProductException extends RuntimeException {

    public ProductException(String message, Throwable cause) {
        super(message, cause);
    }

    @SuppressWarnings("unused")
    public ProductException(String message) {
        super(message);
    }
}
