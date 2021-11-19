package ru.spring.app.engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PostNotFoundException extends Throwable {
    public PostNotFoundException(String message) {
        super(message);
    }
}
