package ru.spring.app.engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.spring.app.engine.api.response.errors.ApiErrorResponse;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({PostNotFoundException.class, CaptchaNotFoundException.class, RegistrationFailedException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(Exception exception) {
        return new ResponseEntity<>(new ApiErrorResponse(HttpStatus.NOT_FOUND,
                "not found", exception), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessIsDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessIsDeniedException(Exception exception) {
        return new ResponseEntity<>(new ApiErrorResponse(HttpStatus.UNAUTHORIZED,
                "Access is denied", exception), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({AddCommentFailException.class, AddPostFailException.class})
    public ResponseEntity<ApiErrorResponse> handleAddCommentFailException(Exception exception) {
        return new ResponseEntity<>(new ApiErrorResponse(HttpStatus.BAD_REQUEST,
                "bad request", exception), HttpStatus.BAD_REQUEST);
    }

}
