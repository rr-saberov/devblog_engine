package ru.spring.app.engine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public record AddPostRequest(
        long timestamp,
        @JsonProperty("active") int isActive,
        String title,
        List<String> tags,
        String text
) {}
