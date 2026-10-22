
package com.logistics.identity.infrastructure.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class SmtpPasswordResetEmailSenderTest {

    private JavaMailSender mailSender;
    private SmtpPasswordResetEmailSender emailSender;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailSender = new SmtpPasswordResetEmailSender(
                mailSender,
                "no-reply@example.com",
                "https://app.example.com/reset-password");
    }

    @Test
    void shouldSendPasswordResetEmailWithEncodedTokenAndExpiry() {
        Instant expiresAt = Instant.parse("2026-10-22T12:15:00Z");
        String rawToken = "token+with/special=characters";

        emailSender.sendPasswordResetEmail(
                "user@example.com", rawToken, expiresAt);

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals("no-reply@example.com", message.getFrom());
        assertArrayEquals(new String[] {"user@example.com"}, message.getTo());
        assertEquals(
                "Reset your Real-Time Logistics password",
                message.getSubject());

        assertNotNull(message.getText());
        assertTrue(message.getText().contains(
                "token=token%2Bwith%2Fspecial%3Dcharacters"));
        assertTrue(message.getText().contains(expiresAt.toString()));
    }

    @Test
    void shouldRejectBlankRecipientWithoutSendingEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> emailSender.sendPasswordResetEmail(
                        " ", "some-token", Instant.now()));

        verifyNoInteractions(mailSender);
    }

    @Test
    void shouldRejectBlankTokenWithoutSendingEmail() {
        assertThrows(
                IllegalArgumentException.class,
                () -> emailSender.sendPasswordResetEmail(
                        "user@example.com", " ", Instant.now()));

        verifyNoInteractions(mailSender);
    }
}
