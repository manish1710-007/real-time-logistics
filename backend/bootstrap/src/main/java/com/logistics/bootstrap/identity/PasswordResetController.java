
package com.logistics.bootstrap.identity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.logistics.identity.application.service.PasswordResetService;
import com.logistics.identity.domain.repository.TenantRepository;
import com.logistics.identity.domain.valueobject.TenantStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/auth/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;
    private final TenantRepository tenantRepository;

    public PasswordResetController(
            PasswordResetService passwordResetService,
            TenantRepository tenantRepository) {
        this.passwordResetService = passwordResetService;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping("/request")
    public ResponseEntity<MessageResponse> requestReset(
            @Valid @RequestBody ResetRequest request) {

        tenantRepository.findBySlug(request.tenantSlug().trim())
                .filter(tenant -> tenant.status() == TenantStatus.ACTIVE)
                .ifPresent(tenant -> passwordResetService.requestPasswordReset(
                        tenant.id(), request.email()));

        return ResponseEntity.ok(new MessageResponse(
                "If the account is eligible, password-reset instructions will be sent."));
    }

    @PostMapping("/confirm")
    public ResponseEntity<MessageResponse> confirmReset(
            @Valid @RequestBody ResetConfirmation request) {

        passwordResetService.resetPassword(request.token(), request.newPassword());

        return ResponseEntity.ok(
                new MessageResponse("Password has been reset successfully."));
    }

    public record ResetRequest(
            @NotBlank @Size(max = 100) String tenantSlug,
            @NotBlank @Size(max = 254) String email) {}

    public record ResetConfirmation(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 128) String newPassword) {}

    public record MessageResponse(String message) {}
}
