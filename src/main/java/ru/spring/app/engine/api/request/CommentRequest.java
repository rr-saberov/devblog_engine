package ru.spring.app.engine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;


public record CommentRequest(
        @JsonProperty("parent_id") long parentId,
        @JsonProperty("post_id") long postId,
        String text
) {}
