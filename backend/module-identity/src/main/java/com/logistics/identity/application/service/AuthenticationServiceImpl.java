package com.logistics.identity.application.service;

import com.logistics.identity.application.command.AuthenticateUserCommand;
import com.logistics.identity.domain.entity.Role;
import com.logistics.identity.domain.entity.Tenant;
import com.logistics.identity.domain.entity.User;
import com.logistics.identity.domain.repository.TenantRepository;
import com.logistics.identity.domain.repository.UserRepository;
import com.logistics.identity.domain.service.PasswordHasher;
import com.logistics.identity.domain.valueobject.TenantStatus;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String AUTHENTICATION_FAILED = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TenantRepository tenantRepository;
    private final RoleLoader roleLoader;
    private final PermissionLoader permissionLoader;

    public AuthenticationServiceImpl(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TenantRepository tenantRepository,
            RoleLoader roleLoader,
            PermissionLoader permissionLoader) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tenantRepository = tenantRepository;
        this.roleLoader = roleLoader;
        this.permissionLoader = permissionLoader;
    }

    @Override
    public AuthenticationResult authenticate(AuthenticateUserCommand command) {
        User user = userRepository
                .findByTenantIdAndEmail(command.tenantId(), command.email())
                .orElseThrow(() -> new IllegalArgumentException(AUTHENTICATION_FAILED));

        validateAccountStatus(user);

        if (!passwordHasher.matches(command.password(), user.passwordHash())) {
            throw new IllegalArgumentException(AUTHENTICATION_FAILED);
        }

        Tenant tenant = tenantRepository
                .findById(command.tenantId())
                .filter(t -> t.status() == TenantStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException(AUTHENTICATION_FAILED));

        List<Role> roles = roleLoader.loadRole(user.id());
        var permissions = permissionLoader.loadPermissions(roles);

        throw new UnsupportedOperationException("Authentication token generation is not implemented yet");
    }

    private void validateAccountStatus(User user) {
        switch (user.status()) {
            case INVITED, ACTIVE -> {
                // Authentication may proceed.
            }

            case SUSPENDED, LOCKED, DISABLED, DELETED -> throw new IllegalArgumentException(AUTHENTICATION_FAILED);
        }
    }
}
