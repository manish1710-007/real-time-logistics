package com.logistics.identity.application.service;

import com.logistics.identity.domain.entity.Permission;
import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.repository.PermissionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PermissionLoaderImpl implements PermissionLoader {

    private final PermissionRepository permissionRepository;

    public PermissionLoaderImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<Permission> loadPermissions(List<Role> roles) {
        return roles.stream()
                .flatMap(role -> role.permissionIds().stream())
                .map(permissionRepository::findById)
                .flatMap(java.util.Optional::stream)
                .toList();
    }
}
