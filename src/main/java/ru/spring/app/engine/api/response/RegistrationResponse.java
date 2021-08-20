package ru.spring.app.engine.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import ru.spring.app.engine.api.response.errors.RegistrationProfileError;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegistrationResponse {

    private boolean result;
    private List<RegistrationProfileError> errors;

    public RegistrationResponse(boolean result) {
        this.result = result;
    }
}
