package user_service.domain.model;

import common.ddd.ValueObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Password implements ValueObject {

    private final String hash;

    public Password(String value) {
        if (!isStrongEnough(value)) {
            throw new IllegalArgumentException("Weak password: must contain at least 8 characters, 1 digit, 1 upper-case letter.");
        }
        this.hash = hashPassword(value);
    }

    public String value() {
        return hash;
    }

    public boolean matches(Password other) {
        return this.hash.equals(other.hash);
    }

    /** Hash SHA-256 + Base64 */
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private boolean isStrongEnough(String password) {
        return password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[0-9].*");
    }

    public static Password is(String password) {
        return new Password(password);
    }
}
