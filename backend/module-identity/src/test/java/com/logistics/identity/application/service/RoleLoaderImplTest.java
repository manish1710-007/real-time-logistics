package com.logistics.identity.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.repository.RoleRepository;
import com.logistics.identity.domain.repository.UserRoleRepository;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.UserId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoleLoaderImplTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private RoleRepository roleRepository;

    private RoleLoaderImpl roleLoader;

    @BeforeEach
    void setUp() {
        roleLoader = new RoleLoaderImpl(userRoleRepository, roleRepository);
    }

    @Test
    void shouldLoadRolesForUser() {
        UserId userId = new UserId(UUID.randomUUID());

        RoleId roleId1 = new RoleId(UUID.randomUUID());
        RoleId roleId2 = new RoleId(UUID.randomUUID());

        Role role1 = createRole(roleId1, "ADMIN");
        Role role2 = createRole(roleId2, "DISPATCHER");

        when(userRoleRepository.findRoleIdsByUserId(userId)).thenReturn(List.of(roleId1, roleId2));

        when(roleRepository.findById(roleId1)).thenReturn(Optional.of(role1));

        when(roleRepository.findById(roleId2)).thenReturn(Optional.of(role2));

        List<Role> result = roleLoader.loadRole(userId);

        assertEquals(List.of(role1, role2), result);

        verify(userRoleRepository).findRoleIdsByUserId(userId);

        verify(roleRepository).findById(roleId1);

        verify(roleRepository).findById(roleId2);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoRoles() {
        UserId userId = new UserId(UUID.randomUUID());

        when(userRoleRepository.findRoleIdsByUserId(userId)).thenReturn(List.of());

        List<Role> result = roleLoader.loadRole(userId);

        assertEquals(List.of(), result);

        verify(userRoleRepository).findRoleIdsByUserId(userId);
    }

    @Test
    void shouldSkipMissingRoles() {
        UserId userId = new UserId(UUID.randomUUID());

        RoleId existingRoleId = new RoleId(UUID.randomUUID());
        RoleId missingRoleId = new RoleId(UUID.randomUUID());

        Role existingRole = createRole(existingRoleId, "ADMIN");

        when(userRoleRepository.findRoleIdsByUserId(userId)).thenReturn(List.of(existingRoleId, missingRoleId));

        when(roleRepository.findById(existingRoleId)).thenReturn(Optional.of(existingRole));

        when(roleRepository.findById(missingRoleId)).thenReturn(Optional.empty());

        List<Role> result = roleLoader.loadRole(userId);

        assertEquals(List.of(existingRole), result);

        verify(roleRepository).findById(existingRoleId);

        verify(roleRepository).findById(missingRoleId);
    }

    private Role createRole(RoleId roleId, String name) {
        return Role.reconstitute(
                roleId,
                null,
                name,
                com.logistics.identity.domain.valueobject.RoleType.SYSTEM,
                java.util.Set.of(),
                java.time.Instant.now(),
                java.time.Instant.now());
    }
}
