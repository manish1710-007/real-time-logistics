package com.logistics.shared.domain.entity;

import jakarta.persistence.Embeddable;
import java.time.Instant;
import java.util.UUID;

@Embeddable
public record AuditInfo(
    Instant createdAt,
    UUID createdBy,
    Instant updatedAt,
    UUID updatedBy
) {
    public static AuditInfo create(UUID actor) {
        return new AuditInfo(Instant.now(), actor, Instant.now(), actor);
    }

    public AuditInfo touch(UUID actor) {
        return new AuditInfo(createdAt, createdBy, Instant.now(), actor);
    }
}