package dev.mmatokovic.pantrio.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="The URI is invalid or the resource requested does not exist")
public class NotFoundException extends RuntimeException{
    public NotFoundException() {
        super("Not Found");
    }
}
