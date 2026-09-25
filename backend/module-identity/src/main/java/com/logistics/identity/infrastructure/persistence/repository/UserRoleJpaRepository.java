package com.logistics.identity.infrastructure.persistence.repository;

import com.logistics.identity.infrastructure.persistence.entity.UserRoleEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleJpaRepository extends JpaRepository<UserRoleEntity, UserRoleEntity.UserRoleId> {

    List<UserRoleEntity> findByUserId(UUID userId);

    boolean existsByUserIdAndRoleId(UUID userId, UUID roleId);

    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);
}
