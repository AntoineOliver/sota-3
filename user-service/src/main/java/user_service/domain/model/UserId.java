package user_service.domain.model;

import common.ddd.ValueObject;

import java.util.Objects;
import java.util.Random;

public record UserId(String value) implements ValueObject {

    public UserId {
        Objects.requireNonNull(value, "UserId cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("UserId cannot be empty");
        }

        if (!value.matches("^[A-Za-z0-9._-]+-[0-9]{3}$")) {
            throw new IllegalArgumentException(
                    "Invalid UserId format. Expected: name-XYZ (XYZ = 3 digits). Got: " + value
            );
        }
    }


    public static UserId is(String userId) {
        return new UserId(userId);
    }


    /**
     * Generate a UserId based on username + random 3-digit key.
     */
    public static UserId fromUsername(String username) {
        Objects.requireNonNull(username, "username cannot be null");

        if (!username.matches("^[A-Za-z0-9._-]+$")) {
            throw new IllegalArgumentException(
                    "Username can only contain letters, digits, '.', '_', '-'"
            );
        }

        int key = new Random().nextInt(1000);
        String keyFormatted = String.format("%03d", key);

        return new UserId(username + "-" + keyFormatted);
    }
}
