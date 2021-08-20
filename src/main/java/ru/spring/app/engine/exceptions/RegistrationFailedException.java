package ru.spring.app.engine.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class RegistrationFailedException extends Throwable {
    public RegistrationFailedException(String message) {
        super(message);
    }
}
