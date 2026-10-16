package com.logistics.shared.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldSetAndRetrieveTenantId() {
        UUID tenantId = UUID.randomUUID();

        TenantContext.setTenantId(tenantId);

        assertEquals(tenantId, TenantContext.getTenantId());
    }

    @Test
    void shouldRejectNullTenantId() {
        assertThrows(IllegalArgumentException.class, () -> TenantContext.setTenantId(null));
    }

    @Test
    void shouldThrowWhenTenantContextIsMissing() {
        assertThrows(IllegalArgumentException.class, TenantContext::getTenantId);
    }

    @Test
    void shouldReturnNullWhenTenantContextIsMissing() {
        assertNull(TenantContext.getTenantIdOrNull());
    }

    @Test
    void shouldClearTenantContext() {
        TenantContext.setTenantId(UUID.randomUUID());

        TenantContext.clear();

        assertNull(TenantContext.getTenantIdOrNull());
    }

    @Test
    void shouldReplacePreviousTenantId() {
        UUID firstTenantId = UUID.randomUUID();
        UUID secondTenantId = UUID.randomUUID();

        TenantContext.setTenantId(firstTenantId);
        TenantContext.setTenantId(secondTenantId);

        assertEquals(secondTenantId, TenantContext.getTenantId());
    }
}
