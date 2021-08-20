package ru.spring.app.engine.api.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostRequest {
    private int offset;
    private int limit;
    private String mode;
}
