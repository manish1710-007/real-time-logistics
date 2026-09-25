package com.logistics.identity.domain.repository;

import com.logistics.identity.domain.entity.UserRole;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.UserId;
import java.util.List;

public interface UserRoleRepository {

    UserRole save(UserRole userRole);

    List<RoleId> findRoleIdsByUserId(UserId userId);

    boolean exists(UserId userId, RoleId roleId);

    void delete(UserId userId, RoleId roleId);
}
