package ru.spring.app.engine.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.spring.app.engine.api.response.enums.ResultStatus;

@Data
@AllArgsConstructor
public class OkResult {
    private ResultStatus result;
}
