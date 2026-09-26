package com.logistics.identity.application.service;

import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.valueobject.UserId;
import java.util.List;

public interface RoleLoader {
    List<Role> loadRole(UserId userId);
}
