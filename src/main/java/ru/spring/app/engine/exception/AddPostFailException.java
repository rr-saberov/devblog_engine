package ru.spring.app.engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AddPostFailException extends Throwable {
    public AddPostFailException(String message) {
        super(message);
    }
}
