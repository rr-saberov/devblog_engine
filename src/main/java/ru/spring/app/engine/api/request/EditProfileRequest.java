package ru.spring.app.engine.api.request;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record EditProfileRequest (
        String name,
        String email,
        String password,
        Integer removePhoto,
        String photo
) {
    public EditProfileRequest(String name, String email, String password, Integer removePhoto) {
        this(name, email, password, removePhoto, null);
    }
}

