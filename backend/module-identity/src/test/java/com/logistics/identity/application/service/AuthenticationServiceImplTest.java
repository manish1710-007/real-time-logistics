package com.logistics.identity.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.logistics.identity.application.command.AuthenticateUserCommand;
import com.logistics.identity.domain.entity.Tenant;
import com.logistics.identity.domain.entity.User;
import com.logistics.identity.domain.repository.TenantRepository;
import com.logistics.identity.domain.repository.UserRepository;
import com.logistics.identity.domain.service.PasswordHasher;
import com.logistics.identity.domain.valueobject.TenantId;
import com.logistics.identity.domain.valueobject.TenantStatus;
import com.logistics.identity.domain.valueobject.UserId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationServiceImpl(userRepository, passwordHasher, tenantRepository);
    }

    @Test
    void shouldRejectAuthenticationWhenTenantDoesNotExist() {
        TenantId tenantId = new TenantId(UUID.randomUUID());

        User user = createActiveUser(tenantId);

        AuthenticateUserCommand command =
                new AuthenticateUserCommand(tenantId, "User@Example.com", "SecurePassword123!");
        when(userRepository.findByTenantIdAndEmail(tenantId, "user@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordHasher.matches("SecurePassword123!", user.passwordHash())).thenReturn(true);

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> authenticationService.authenticate(command));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void shouldRejectAuthenticationWhenTenantIsNotActive() {
        TenantId tenantId = new TenantId(UUID.randomUUID());

        User user = createActiveUser(tenantId);

        AuthenticateUserCommand command =
                new AuthenticateUserCommand(tenantId, "user@example.com", "SecurePassword123!");
        when(userRepository.findByTenantIdAndEmail(tenantId, "user@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordHasher.matches("SecurePassword123!", user.passwordHash())).thenReturn(true);

        Tenant tenant = Tenant.reconstitute(
                tenantId, "Test Tenant", "test-tenant", TenantStatus.SUSPENDED, Instant.now(), Instant.now());

        when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> authenticationService.authenticate(command));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void shouldRejectUnknownUser() {
        TenantId tenantId = new TenantId(UUID.randomUUID());

        AuthenticateUserCommand command =
                new AuthenticateUserCommand(tenantId, "user@example.com", "SecurePassword123!");

        when(userRepository.findByTenantIdAndEmail(tenantId, "user@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> authenticationService.authenticate(command));

        assertEquals("Invalid email or password", exception.getMessage());

        verify(passwordHasher, never()).matches(anyString(), anyString());
    }

    @Test
    void shouldRejectIncorrectPassword() {
        TenantId tenantId = new TenantId(UUID.randomUUID());

        User user = createActiveUser(tenantId);

        AuthenticateUserCommand command =
                new AuthenticateUserCommand(tenantId, "user@example.com", "WrongPassword123!");

        when(userRepository.findByTenantIdAndEmail(tenantId, "user@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordHasher.matches("WrongPassword123!", user.passwordHash())).thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> authenticationService.authenticate(command));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void shouldRejectSuspendedUser() {
        TenantId tenantId = new TenantId(UUID.randomUUID());

        User user = createActiveUser(tenantId);
        user.suspend();

        AuthenticateUserCommand command =
                new AuthenticateUserCommand(tenantId, "user@example.com", "SecurePassword123!");

        when(userRepository.findByTenantIdAndEmail(tenantId, "user@example.com"))
                .thenReturn(Optional.of(user));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> authenticationService.authenticate(command));

        assertEquals("Invalid email or password", exception.getMessage());

        verify(passwordHasher, never()).matches(anyString(), anyString());
    }

    @Test
    void shouldNotRevealDisabledAccount() {
        TenantId tenantId = new TenantId(UUID.randomUUID());

        User user = createActiveUser(tenantId);
        user.disable();

        AuthenticateUserCommand command =
                new AuthenticateUserCommand(tenantId, "user@example.com", "SecurePassword123!");

        when(userRepository.findByTenantIdAndEmail(tenantId, "user@example.com"))
                .thenReturn(Optional.of(user));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> authenticationService.authenticate(command));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    private User createActiveUser(TenantId tenantId) {
        return User.reconstitute(
                UserId.generate(),
                tenantId,
                "user@example.com",
                "$argon2id$v=19$m=65536,t=3,p=1$hash",
                "Test",
                "User",
                null,
                com.logistics.identity.domain.valueobject.UserStatus.ACTIVE,
                true,
                null,
                Instant.now(),
                Instant.now(),
                0L);
    }
}
