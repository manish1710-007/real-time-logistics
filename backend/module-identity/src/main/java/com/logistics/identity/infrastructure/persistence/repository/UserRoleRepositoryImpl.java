package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.domain.entity.UserRole;
import com.logistics.identity.domain.repository.UserRoleRepository;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.infrastructure.persistence.entity.UserRoleEntity;
import com.logistics.identity.infrastructure.persistence.mapper.UserRoleMapper;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UserRoleRepositoryImpl implements UserRoleRepository {

    private final UserRoleJpaRepository jpaRepository;
    private final UserRoleMapper mapper;

    public UserRoleRepositoryImpl(UserRoleJpaRepository jpaRepository, UserRoleMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public UserRole save(UserRole userRole) {
        UserRoleEntity entity = mapper.toEntity(userRole);
        UserRoleEntity saved = jpaRepository.save(entity);

        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleId> findRoleIdsByUserId(UserId userId) {
        return jpaRepository.findByUserId(userId.value()).stream()
                .map(UserRoleEntity::getRoleId)
                .map(RoleId::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(UserId userId, RoleId roleId) {
        return jpaRepository.existsByUserIdAndRoleId(userId.value(), roleId.value());
    }

    @Override
    public void delete(UserId userId, RoleId roleId) {
        jpaRepository.deleteByUserIdAndRoleId(userId.value(), roleId.value());
    }
}
