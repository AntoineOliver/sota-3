package user_service.domain.event;

import java.time.Instant;

public record UserEmailUpdatedEvent(
        String userId,
        String oldEmail,
        String newEmail,
        Instant occurredAt
) implements UserEvent {

    public static UserEmailUpdatedEvent of(String userId, String oldEmail, String newEmail) {
        return new UserEmailUpdatedEvent(userId, oldEmail, newEmail, Instant.now());
    }

    @Override
    public String type() {
        return "UserEmailUpdatedEvent";
    }
}
