package com.faker.receita.infrastructure.web.exception;

import java.time.Clock;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record ErrorResponse(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
    public static ErrorResponse of(int status, String error, String message, String path, Clock clock) {
        return new ErrorResponse(LocalDateTime.now(clock), status, error, message, path);
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        return of(status, error, message, path, Clock.systemDefaultZone());
    }
}
