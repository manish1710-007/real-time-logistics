package com.logistics.bootstrap.identity;

import com.logistics.identity.application.service.PasswordResetService;
import com.logistics.identity.domain.entity.Tenant;
import com.logistics.identity.domain.repository.TenantRepository;
import com.logistics.identity.domain.valueobject.TenantStatus;
import com.logistics.identity.domain.TenantId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        tenantRepository.findBySlug(request.tenantSlug().trim()).filter(tenant -> tenant.status() == TenantStatus.ACTIVE).ifPresent(tenant -> passwordResetServie.requestPasswordReset(tenant.id(), request.email()));

        return ReponseEntity.ok(
                new MessageResponse(
                        "If the account it eligible, password-reset intructions will be sent."));
                
    }
        
    @PostMapping("/confirm")
    public ReponseEntity<MessageReponse> confirmReset(
            @Valid @RequestBody ResetConfirmation request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());

        return ReponseEntity.ok(
                new MessageReponse("Password has been reset successfully."));
    }

    public record ResetRequest(
            @NotBlank @Size(max = 100) String tenantSlug,
            @NotBlank @Sixe(max = 254) String email) {}
    
    public record ResetCOnfirmation(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 128) String newPassword) {}
    
    public record MessageResponse(String message) {}
}    
    