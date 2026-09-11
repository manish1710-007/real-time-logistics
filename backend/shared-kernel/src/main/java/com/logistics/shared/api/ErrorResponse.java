package com.logistics.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success, @JsonProperty("error") ErrorDetail error, String traceId, Instant timestamp) {
    public record ErrorDetail(String code, String message, List<FieldError> fieldErrors) {}

    public record FieldError(String field, String message, Object rejectedValue) {}
}
