package com.logistics.shared.api;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    T data,
    String message,
    Instant timestamp,
    String traceId
) {
    public static <T> ApiResponse<T> success(T data){
        return new ApiResponse<>(true, data, null, Instant.now(), null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, Instant.now(), null);
    }

    public static <T> ApiResponse<T> empty() {
        return new ApiResponse<T>(true, null, null, Instant.now(), null);

    }
}
    

