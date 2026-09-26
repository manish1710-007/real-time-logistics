package com.logistics.identity.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.logistics.identity.domain.entity.UserRole;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.infrastructure.persistence.entity.UserRoleEntity;
import com.logistics.identity.infrastructure.persistence.mapper.UserRoleMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRoleRepositoryImplTest {

    @Mock
    private UserRoleJpaRepository jpaRepository;

    @Mock
    private UserRoleMapper mapper;

    private UserRoleRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new UserRoleRepositoryImpl(jpaRepository, mapper);
    }

    @Test
    void shouldSaveUserRole() {
        UserId userId = new UserId(UUID.randomUUID());
        RoleId roleId = new RoleId(UUID.randomUUID());

        UserRole userRole = UserRole.create(userId, roleId);

        UserRoleEntity entity = new UserRoleEntity(userId.value(), roleId.value());

        when(mapper.toEntity(userRole)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(userRole);

        UserRole result = repository.save(userRole);

        assertSame(userRole, result);

        verify(mapper).toEntity(userRole);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(entity);
    }

    @Test
    void shouldFindRoleIdsByUserId() {
        UUID userId = UUID.randomUUID();
        UUID roleId1 = UUID.randomUUID();
        UUID roleId2 = UUID.randomUUID();

        UserId user = new UserId(userId);

        UserRoleEntity entity1 = new UserRoleEntity(userId, roleId1);
        UserRoleEntity entity2 = new UserRoleEntity(userId, roleId2);

        when(jpaRepository.findByUserId(userId)).thenReturn(List.of(entity1, entity2));

        List<RoleId> result = repository.findRoleIdsByUserId(user);

        assertEquals(List.of(new RoleId(roleId1), new RoleId(roleId2)), result);

        verify(jpaRepository).findByUserId(userId);
    }

    @Test
    void shouldReturnTrueWhenUserHasRole() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        UserId user = new UserId(userId);
        RoleId role = new RoleId(roleId);

        when(jpaRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(true);

        boolean result = repository.exists(user, role);

        assertTrue(result);

        verify(jpaRepository).existsByUserIdAndRoleId(userId, roleId);
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotHaveRole() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        UserId user = new UserId(userId);
        RoleId role = new RoleId(roleId);

        when(jpaRepository.existsByUserIdAndRoleId(userId, roleId)).thenReturn(false);

        boolean result = repository.exists(user, role);

        assertFalse(result);

        verify(jpaRepository).existsByUserIdAndRoleId(userId, roleId);
    }

    @Test
    void shouldDeleteUserRole() {
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        UserId user = new UserId(userId);
        RoleId role = new RoleId(roleId);

        repository.delete(user, role);

        verify(jpaRepository).deleteByUserIdAndRoleId(userId, roleId);
    }
}
