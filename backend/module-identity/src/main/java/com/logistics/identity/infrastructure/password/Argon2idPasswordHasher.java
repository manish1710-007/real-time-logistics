package com.logistics.identity.infrastructure.password;

import com.logistics.identity.domain.service.PasswordHasher;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Component;

@Component
public class Argon2idPasswordHasher implements PasswordHasher {

    private static final int ITERATIONS = 3;
    private static final int MEMORY_KB = 65_536;
    private static final int PARALLELISM = 1;

    private final Argon2 argon2;

    public Argon2idPasswordHasher() {
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }

    @Override
    public String hash(String rawPassword) {
        validatePassword(rawPassword);

        char[] password = rawPassword.toCharArray();

        try {
            return argon2.hash(ITERATIONS, MEMORY_KB, PARALLELISM, password);
        } finally {
            argon2.wipeArray(password);
        }
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        validatePassword(rawPassword);

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be null or blank");
        }

        char[] password = rawPassword.toCharArray();

        try {
            return argon2.verify(passwordHash, password);
        } finally {
            argon2.wipeArray(password);
        }
    }

    private static void validatePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
    }
}
