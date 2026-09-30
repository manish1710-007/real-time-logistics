package com.logistics.identity.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.identity.domain.entity.Permission;
import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.repository.PermissionRepository;
import com.logistics.identity.domain.valueobject.PermissionId;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.RoleType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PermissionLoaderImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    private PermissionLoaderImpl permissionLoader;

    @BeforeEach
    void setUp() {
        permissionLoader = new PermissionLoaderImpl(permissionRepository);
    }

    @Test
    void shouldLoadPermissionsFromRoles() {
        RoleId roleId = new RoleId(UUID.randomUUID());

        PermissionId permissionId1 = new PermissionId(UUID.randomUUID());

        PermissionId permissionId2 = new PermissionId(UUID.randomUUID());

        Permission permission1 = Permission.reconstitute(permissionId1, "order:read", "Read orders");

        Permission permission2 = Permission.reconstitute(permissionId2, "order:create", "Create orders");

        Role role = Role.reconstitute(
                roleId,
                null,
                "ADMIN",
                RoleType.SYSTEM,
                Set.of(permissionId1, permissionId2),
                Instant.now(),
                Instant.now());

        when(permissionRepository.findById(permissionId1)).thenReturn(Optional.of(permission1));

        when(permissionRepository.findById(permissionId2)).thenReturn(Optional.of(permission2));

        List<Permission> result = permissionLoader.loadPermissions(List.of(role));

        assertEquals(List.of(permission1, permission2), result);

        verify(permissionRepository).findById(permissionId1);

        verify(permissionRepository).findById(permissionId2);
    }

    @Test
    void shouldReturnEmptyListWhenRolesHaveNoPermissions() {
        RoleId roleId = new RoleId(UUID.randomUUID());

        Role role = Role.reconstitute(roleId, null, "ADMIN", RoleType.SYSTEM, Set.of(), Instant.now(), Instant.now());

        List<Permission> result = permissionLoader.loadPermissions(List.of(role));

        assertEquals(List.of(), result);
    }

    @Test
    void shouldReturnEmptyListWhenNoRolesAreProvided() {
        List<Permission> result = permissionLoader.loadPermissions(List.of());

        assertEquals(List.of(), result);
    }

    @Test
    void shouldSkipMissingPermissions() {
        RoleId roleId = new RoleId(UUID.randomUUID());

        PermissionId existingPermissionId = new PermissionId(UUID.randomUUID());

        PermissionId missingPermissionId = new PermissionId(UUID.randomUUID());

        Permission permission = Permission.reconstitute(existingPermissionId, "order:read", "Read orders");

        Role role = Role.reconstitute(
                roleId,
                null,
                "ADMIN",
                RoleType.SYSTEM,
                Set.of(existingPermissionId, missingPermissionId),
                Instant.now(),
                Instant.now());

        when(permissionRepository.findById(existingPermissionId)).thenReturn(Optional.of(permission));

        when(permissionRepository.findById(missingPermissionId)).thenReturn(Optional.empty());

        List<Permission> result = permissionLoader.loadPermissions(List.of(role));

        assertEquals(List.of(permission), result);

        verify(permissionRepository).findById(existingPermissionId);

        verify(permissionRepository).findById(missingPermissionId);
    }
}
