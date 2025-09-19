package com.library.flow.common.dto;

import java.time.Instant;
import java.util.Map;

public record AppResponse<T>(
        boolean success,
        int status,
        String message,
        Instant timestamp,
        T data
) {
    public static <T> AppResponse<T> ok(T data) {
        return new AppResponse<>(true, 200, "OK", Instant.now(), data);
    }
    public static <T> AppResponse<T> created(T data) {
        return new AppResponse<>(true, 201, "Created", Instant.now(), data);
    }
    public static <T> AppResponse<T> of(int status, String message, T data) {
        return new AppResponse<>(status < 400, status, message, Instant.now(), data);
    }
    public static AppResponse<Void> error(int status, String message) {
        return new AppResponse<>(false, status, message, Instant.now(), null);
    }
}
