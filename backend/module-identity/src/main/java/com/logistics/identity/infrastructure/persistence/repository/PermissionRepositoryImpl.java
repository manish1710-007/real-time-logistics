package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.domain.entity.Permission;
import com.logistics.identity.domain.repository.PermissionRepository;
import com.logistics.identity.domain.valueobject.PermissionId;
import com.logistics.identity.infrastructure.persistence.entity.PermissionEntity;
import com.logistics.identity.infrastructure.persistence.mapper.PermissionMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionJpaRepository permissionJpaRepository;

    public PermissionRepositoryImpl(
        PermissionJpaRepository permissionJpaRepository
    ) {
        this.permissionJpaRepository = permissionJpaRepository;
    }

    @Override
    @Transactional
    public Permission save(Permission permission) {
        if (permission == null) { 
            throw new IllegalArgumentException("Permission cannot be null");
    
        }

        PermissionEntity entity = PermissionMapper.toEntity(permission);
        PermissionEntity savedEntity = permissionJpaRepository.save(entity);

        return PermissionMapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Permission> findById(PermissionId permissionId) {
        if (permissionId == null) {
            throw new IllegalArgumentException("Permission ID cannot be null");
        }

        return permissionJpaRepository
                .findById(permissionId.value())
                .map(PermissionMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Permission> findByCode(String code) {
        validateCode(code);

        return permissionJpaRepository
                .findByCode(code)
                .map(PermissionMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        validateCode(code);

        return permissionJpaRepository.existsByCode(code);
    }

    private static void validateCode(String code){
        if (code == null || code.isBlank()){
            throw new IllegalArgumentException(
                "Permission code cannot be null or blank");
        }
    }
}