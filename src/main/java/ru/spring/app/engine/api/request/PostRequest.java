package ru.spring.app.engine.api.request;

public record PostRequest(
        int offset,
        int limit,
        String mode
) {}
