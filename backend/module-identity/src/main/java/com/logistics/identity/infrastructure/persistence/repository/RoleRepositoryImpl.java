package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.repository.RoleRepository;
import com.logistics.identity.domain.valueobject.PermissionId;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.infrastructure.persistence.entity.RoleEntity;
import com.logistics.identity.infrastructure.persistence.entity.RolePermissionEntity;
import com.logistics.identity.infrastructure.persistence.mapper.RoleMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;
    private final RolePermissionJpaRepository rolePermissionJpaRepository;

    public RoleRepositoryImpl(
            RoleJpaRepository roleJpaRepository,
            RolePermissionJpaRepository rolePermissionJpaRepository
    ) {
        this.roleJpaRepository = roleJpaRepository;
        this.rolePermissionJpaRepository = rolePermissionJpaRepository;
    }

    @Override
    @Transactional
    public Role save(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        RoleEntity entity = RoleMapper.toEntity(role);
        RoleEntity savedEntity = roleJpaRepository.save(entity);

        UUID roleId = savedEntity.getId();

        // Keep the join table synchronized with the domain Role.
        rolePermissionJpaRepository.deleteByRoleId(roleId);

        role.permissionIds().stream()
                .map(permissionId ->
                        new RolePermissionEntity(
                                roleId,
                                permissionId.value()
                        )
                )
                .forEach(rolePermissionJpaRepository::save);

        return RoleMapper.toDomain(
                savedEntity,
                role.permissionIds()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(RoleId roleId) {
        if (roleId == null) {
            throw new IllegalArgumentException("Role ID cannot be null");
        }

        return roleJpaRepository.findById(roleId.value())
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByTenantIdAndName(
            TenantId tenantId,
            String name
    ) {
        validateName(name);

        Optional<RoleEntity> entity;

        if (tenantId == null) {
            entity = roleJpaRepository.findByTenantIdIsNullAndName(name);
        } else {
            entity = roleJpaRepository.findByTenantIdAndName(
                    tenantId.value(),
                    name
            );
        }

        return entity.map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByTenantIdAndName(
            TenantId tenantId,
            String name
    ) {
        validateName(name);

        if (tenantId == null) {
            return roleJpaRepository.existsByTenantIdIsNullAndName(name);
        }

        return roleJpaRepository.existsByTenantIdAndName(
                tenantId.value(),
                name
        );
    }

    private Role toDomain(RoleEntity entity) {
        Set<PermissionId> permissionIds =
                rolePermissionJpaRepository.findByRoleId(entity.getId())
                        .stream()
                        .map(RolePermissionEntity::getPermissionId)
                        .map(PermissionId::new)
                        .collect(Collectors.toSet());

        return RoleMapper.toDomain(entity, permissionIds);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Role name cannot be null or blank"
            );
        }
    }
}