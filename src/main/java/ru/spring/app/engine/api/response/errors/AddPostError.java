package ru.spring.app.engine.api.response.errors;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddPostError {
    private String title;
    private String text;
}
