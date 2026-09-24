package com.logistics.identity.application.service;

import com.logistics.identity.application.command.AuthenticateUserCommand;
import com.logistics.identity.domain.entity.User;
import com.logistics.identity.domain.repository.TenantRepository;
import com.logistics.identity.domain.repository.UserRepository;
import com.logistics.identity.domain.service.PasswordHasher;
import com.logistics.identity.domain.valueobject.TenantStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String AUTHENTICATION_FAILED = "Invalid email or password";

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    private final TenantRepository tenantRepository;

    public AuthenticationServiceImpl(
            UserRepository userRepository, PasswordHasher passwordHasher, TenantRepository tenantRepository) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tenantRepository = tenantRepository;
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

        tenantRepository
                .findById(command.tenantId())
                .filter(tenant -> tenant.status() == TenantStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException(AUTHENTICATION_FAILED));

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
