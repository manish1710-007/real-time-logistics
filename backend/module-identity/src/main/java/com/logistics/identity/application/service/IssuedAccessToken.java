package com.logistics.identity.application.service;

import java.time.Instant;

public record IssuedAccessToken(String token, Instant expiresAt) {}
