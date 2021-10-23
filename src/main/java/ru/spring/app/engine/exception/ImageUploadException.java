package ru.spring.app.engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ImageUploadException extends Throwable {
    public ImageUploadException(String message) {
        super(message);
    }
}
