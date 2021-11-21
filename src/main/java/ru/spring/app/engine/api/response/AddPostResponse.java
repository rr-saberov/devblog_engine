package ru.spring.app.engine.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.spring.app.engine.api.response.errors.AddPostError;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
public class AddPostResponse {
    private boolean result;
    private List<AddPostError> errors;

    public AddPostResponse(boolean result) {
        this.result = result;
    }
}
