package user_service.domain.event;

import java.time.Instant;

public record UserRegisteredEvent(
        String userId,
        String username,
        String email,
        Instant occurredAt
) implements UserEvent {

    public static UserRegisteredEvent of(String userId, String username, String email) {
        return new UserRegisteredEvent(userId, username, email, Instant.now());
    }

    @Override
    public String type() {
        return "UserRegisteredEvent";
    }
}
