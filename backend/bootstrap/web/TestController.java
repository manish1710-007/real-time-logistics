package com.logistics.bootstrap.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.shared.api.ApiResponse;
import com.logistics.shared.exception.NotFoundException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    // Test successful response
    @GetMapping("/success")
    public ApiResponse<String> getSuccess() {
        return ApiResponse.success("Hello World", "Operation completed");
    }

    // Test validation error
    @PostMapping("/validate")
    public ApiResponse<String> postValidate(@Valid @RequestBody TestRequest request) {
        return ApiResponse.success(request.name());
    }

    // Test business error
    @GetMapping("/notfound")
    public ApiResponse<String> getNotFound() {
        throw new NotFoundException("Driver", "123");
    }

    // Nested record
    public record TestRequest(
            @NotBlank(message = "Name cannot be blank") String name
    ) {}
}