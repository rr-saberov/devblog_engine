package ru.spring.app.engine.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AddCommentFailException extends Throwable {
    public AddCommentFailException(String message) {
        super(message);
    }
}
