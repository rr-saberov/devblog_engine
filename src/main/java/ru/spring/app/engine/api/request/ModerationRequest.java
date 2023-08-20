package ru.spring.app.engine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ModerationRequest(
        @JsonProperty("post_id")
        long postId,
        String decision
) {}
