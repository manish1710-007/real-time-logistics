package com.logistics.identity.domain.entity;

import static org.junit.jupiter.api.Assertions.*;

import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.TenantStatus;
import org.junit.jupiter.api.Test;

class TenantTest {

    @Test
    void shouldCreateActiveTenant() {
        Tenant tenant = Tenant.create(TenantId.generate(), "ACME Logistics", "acme-logistics");

        assertNotNull(tenant.id());
        assertEquals("ACME Logistics", tenant.name());
        assertEquals("acme-logistics", tenant.slug());
        assertEquals(TenantStatus.ACTIVE, tenant.status());
        assertNotNull(tenant.createdAt());
        assertNotNull(tenant.updatedAt());
    }

    @Test
    void shouldRejectNullTenantId() {
        assertThrows(NullPointerException.class, () -> Tenant.create(null, "ACME Logistics", "acme-logistics"));
    }

    @Test
    void shouldRejectBlankTenantName() {
        assertThrows(IllegalArgumentException.class, () -> Tenant.create(TenantId.generate(), "   ", "acme-logistics"));
    }

    @Test
    void shouldRejectBlankTenantSlug() {
        assertThrows(IllegalArgumentException.class, () -> Tenant.create(TenantId.generate(), "ACME Logistics", "   "));
    }

    @Test
    void shouldSuspendTenant() {
        Tenant tenant = Tenant.create(TenantId.generate(), "ACME Logistics", "acme-logistics");

        tenant.suspend();

        assertEquals(TenantStatus.SUSPENDED, tenant.status());
    }

    @Test
    void shouldActivateTenant() {
        Tenant tenant = Tenant.create(TenantId.generate(), "ACME Logistics", "acme-logistics");

        tenant.suspend();
        tenant.activate();

        assertEquals(TenantStatus.ACTIVE, tenant.status());
    }

    @Test
    void shouldDisableTenant() {
        Tenant tenant = Tenant.create(TenantId.generate(), "ACME Logistics", "acme-logistics");

        tenant.disable();

        assertEquals(TenantStatus.DISABLED, tenant.status());
    }

    @Test
    void shouldRenameTenant() {
        Tenant tenant = Tenant.create(TenantId.generate(), "ACME Logistics", "acme-logistics");

        tenant.rename("ACME Transport");

        assertEquals("ACME Transport", tenant.name());
    }

    @Test
    void shouldChangeTenantSlug() {
        Tenant tenant = Tenant.create(TenantId.generate(), "ACME Logistics", "acme-logistics");

        tenant.changeSlug("acme-transport");

        assertEquals("acme-transport", tenant.slug());
    }
}
