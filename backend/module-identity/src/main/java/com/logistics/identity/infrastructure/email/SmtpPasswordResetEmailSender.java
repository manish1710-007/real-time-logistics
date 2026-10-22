
package com.logistics.identity.infrastructure.email;

import com.logistics.identity.application.service.PasswordResetEmailSender;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpPasswordResetEmailSender implements PasswordResetEmailSender {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String resetBaseUrl;

    public SmtpPasswordResetEmailSender(
            JavaMailSender mailSender,
            @Value("${identity.email.from}") String fromAddress,
            @Value("${identity.password-reset.base-url}") String resetBaseUrl) {
        this.mailSender = Objects.requireNonNull(mailSender);
        this.fromAddress = requireText(fromAddress, "Email sender address");
        this.resetBaseUrl = requireText(resetBaseUrl, "Password reset base URL");
    }

    @Override
    public void sendPasswordResetEmail(
            String recipientEmail, String rawToken, Instant expiresAt) {

        requireText(recipientEmail, "Recipient email");
        requireText(rawToken, "Reset token");
        Objects.requireNonNull(expiresAt, "Expiry is required");

        String separator = resetBaseUrl.contains("?") ? "&" : "?";
        String resetUrl = resetBaseUrl + separator
                + "token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipientEmail);
        message.setSubject("Reset your Real-Time Logistics password");
        message.setText(
                "We received a request to reset your password.\n\n"
                + "Use this link to choose a new password:\n"
                + resetUrl + "\n\n"
                + "This link expires at " + expiresAt + " (UTC).\n"
                + "If you did not request this, you can ignore this email.");

        mailSender.send(message);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value;
    }
}

