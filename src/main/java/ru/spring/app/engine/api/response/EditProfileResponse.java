package ru.spring.app.engine.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.spring.app.engine.api.response.errors.EditProfileError;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EditProfileResponse {
    private boolean result;
    private List<EditProfileError> errors;
}
