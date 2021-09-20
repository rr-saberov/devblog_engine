package ru.spring.app.engine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AddPostRequest {
    private long timestamp;
    @JsonProperty("active")
    private int isActive;
    private String title;
    private List<String> tags;
    private String text;
}
