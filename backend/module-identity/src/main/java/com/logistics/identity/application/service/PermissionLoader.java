package com.logistics.identity.application.service;

import com.logistics.identity.domain.entity.Permission;
import com.logistics.identity.domain.entity.Role;
import java.util.List;

public interface PermissionLoader {
    List<Permission> loadPermissions(List<Role> roles);
}
