package ru.spring.app.engine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SettingsRequest(
        @JsonProperty("MULTIUSER_MODE")
        boolean multiuserMode,
        @JsonProperty("POST_MODERATION")
        boolean postPremoderation,
        @JsonProperty("STATISTICS_IS_PUBLIC")
        boolean statisticsIsPublic
) {}
