package com.logistics.identity.application.service;

import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.repository.RoleRepository;
import com.logistics.identity.domain.repository.UserRoleRepository;
import com.logistics.identity.domain.valueobject.RoleId;
import com.logistics.identity.domain.valueobject.UserId;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RoleLoaderImpl implements RoleLoader {

    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    public RoleLoaderImpl(UserRoleRepository userRoleRepository, RoleRepository roleRepository) {
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> loadRole(UserId userId) {
        List<RoleId> roleIds = userRoleRepository.findRoleIdsByUserId(userId);

        return roleIds.stream()
                .map(roleRepository::findById)
                .flatMap(java.util.Optional::stream)
                .toList();
    }
}
