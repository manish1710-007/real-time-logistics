package com.logistics.identity.infrastructure.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.logistics.identity.application.service.PasswordResetEmailSender;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class SmtpPasswordResetEmailSenderTest {

    private JavaMailSender mailSender;
    private PasswordResetEmailSender emailSender;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailSender = new SmtpPasswordResetEmailSender(
                mailSender, "no-reply@example.com", "https://app.example.com/reset-password");
    }

    @Test
    void shouldSendPasswordResetEmailWithEncodedTokenAndExpiry() {
        Instant expiresAt = Instant.parse("2026-10-22T15:00:00Z");

        emailSender.sendPasswordResetEmail("user@example.com", "token+with/special=characters", expiresAt);

        org.mockito.ArgumentCaptor<SimpleMailMessage> captor =
                org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("no-reply@example.com", message.getFrom());
        assertEquals("user@example.com", message.getTo()[0]);
        assertEquals("Reset your Real-Time Logistics password", message.getSubject());
        assertTrue(message.getText().contains("token=token%2Bwith%2Fspecial%3Dcharacters"));
        assertTrue(message.getText().contains(expiresAt.toString()));
    }

    @Test
    void shouldRejectBlankRecipientWithoutSendingEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> emailSender.sendPasswordResetEmail(" ", "valid-token", Instant.now()));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldRejectBlankTokenWithoutSendingEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> emailSender.sendPasswordResetEmail("user@example.com", " ", Instant.now()));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
