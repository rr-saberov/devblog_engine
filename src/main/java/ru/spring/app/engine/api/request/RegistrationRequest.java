package ru.spring.app.engine.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RegistrationRequest(
        @JsonProperty("e_mail")
        String email,
        String password,
        String name,
        String captcha,
        @JsonProperty("captcha_secret")
        String captchaSecret
) {}
