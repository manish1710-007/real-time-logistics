package com.logistics.identity.domain.entity;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.logistics.identity.domain.valueobject.PermissionId;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.RoleType;
import com.logistics.identity.domain.valueobject.TenantId;

class RoleTest {

    @Test
    void shouldCreateSystemRole() {
        Role role = Role.createSystemRole(
                RoleId.generate(),
                "Platform Administrator"
        );

        assertEquals(RoleType.SYSTEM, role.type());
        assertNull(role.tenantId());
        assertEquals("Platform Administrator", role.name());
        assertTrue(role.permissionIds().isEmpty());
        assertNotNull(role.createdAt());
        assertNotNull(role.updatedAt());
    }

    @Test
    void shouldCreateTenantRole() {
        TenantId tenantId = TenantId.generate();

        Role role = Role.createTenantRole(
                RoleId.generate(),
                tenantId,
                "Fleet Manager"
        );

        assertEquals(RoleType.TENANT, role.type());
        assertEquals(tenantId, role.tenantId());
        assertEquals("Fleet Manager", role.name());
    }

    @Test
    void shouldRejectNullTenantIdForTenantRole() {
        assertThrows(
                NullPointerException.class,
                () -> Role.createTenantRole(
                        RoleId.generate(),
                        null,
                        "Fleet Manager"
                )
        );
    }

    @Test
    void shouldRejectBlankRoleName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Role.createSystemRole(
                        RoleId.generate(),
                        "   "
                )
        );
    }

    @Test
    void shouldAddPermission() {
        Role role = Role.createSystemRole(
                RoleId.generate(),
                "Platform Administrator"
        );

        PermissionId permissionId = PermissionId.generate();

        role.addPermission(permissionId);

        assertTrue(role.hasPermission(permissionId));
        assertEquals(Set.of(permissionId), role.permissionIds());
    }

    @Test
    void shouldNotAddDuplicatePermission() {
        Role role = Role.createSystemRole(
                RoleId.generate(),
                "Platform Administrator"
        );

        PermissionId permissionId = PermissionId.generate();

        role.addPermission(permissionId);
        role.addPermission(permissionId);

        assertEquals(1, role.permissionIds().size());
    }

    @Test
    void shouldRemovePermission() {
        Role role = Role.createSystemRole(
                RoleId.generate(),
                "Platform Administrator"
        );

        PermissionId permissionId = PermissionId.generate();

        role.addPermission(permissionId);
        role.removePermission(permissionId);

        assertFalse(role.hasPermission(permissionId));
        assertTrue(role.permissionIds().isEmpty());
    }

    @Test
    void shouldRejectNullPermissionId() {
        Role role = Role.createSystemRole(
                RoleId.generate(),
                "Platform Administrator"
        );

        assertThrows(
                NullPointerException.class,
                () -> role.addPermission(null)
        );

        assertThrows(
                NullPointerException.class,
                () -> role.removePermission(null)
        );

        assertThrows(
                NullPointerException.class,
                () -> role.hasPermission(null)
        );
    }

    @Test
    void shouldNotAllowExternalMutationOfPermissions() {
        Role role = Role.createSystemRole(
                RoleId.generate(),
                "Platform Administrator"
        );

        PermissionId permissionId = PermissionId.generate();
        role.addPermission(permissionId);

        Set<PermissionId> permissions = role.permissionIds();

        assertThrows(
                UnsupportedOperationException.class,
                () -> permissions.add(PermissionId.generate())
        );

        assertEquals(1, role.permissionIds().size());
    }
}