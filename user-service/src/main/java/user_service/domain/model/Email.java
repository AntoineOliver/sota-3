package user_service.domain.model;

import common.ddd.ValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) implements ValueObject {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    public Email {
        Objects.requireNonNull(value, "Email cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    public static Email is(String email) {
        return new Email(email);
    }
}
