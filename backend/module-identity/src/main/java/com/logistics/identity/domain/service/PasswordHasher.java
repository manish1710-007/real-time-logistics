package com.logistics.identity.domain.service;

public interface PasswordHasher {
    String hash(String rawPassword);

    boolean mathces(String rawPassword, String passwordHash);
}
