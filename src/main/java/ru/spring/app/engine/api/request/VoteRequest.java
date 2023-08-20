package ru.spring.app.engine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VoteRequest(
        @JsonProperty("post_id")
        Long postId
) {
}
