package com.logistics.identity.infrastructure.persistence.mapper;

import com.logistics.identity.domain.entity.UserRole;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.UserId;
import com.logistics.identity.infrastructure.persistence.entity.UserRoleEntity;
import org.springframework.stereotype.Component;

@Component
public class UserRoleMapper {

    public UserRoleEntity toEntity(UserRole userRole) {
        return new UserRoleEntity(userRole.userId().value(), userRole.roleId().value());
    }

    public UserRole toDomain(UserRoleEntity entity) {
        return UserRole.create(new UserId(entity.getUserId()), new RoleId(entity.getRoleId()));
    }
}
