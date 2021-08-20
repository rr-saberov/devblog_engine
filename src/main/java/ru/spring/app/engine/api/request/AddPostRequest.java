package ru.spring.app.engine.api.request;

import lombok.Data;

import java.util.List;

@Data
public class AddPostRequest {
    private long timestamp;
    private int isActive;
    private String title;
    private List<String> tags;
    private String text;
}
