package ru.spring.app.engine.api.response.errors;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistrationProfileError {
    private String email;
    private String name;
    private String password;
    private String captcha;
}
