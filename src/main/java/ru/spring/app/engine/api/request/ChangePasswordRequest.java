package ru.spring.app.engine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChangePasswordRequest(
        String code,
        String password,
        String captcha,
        @JsonProperty("captcha_secret") String captchaSecret
) {}
