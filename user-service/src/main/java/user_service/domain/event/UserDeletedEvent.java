package user_service.domain.event;

import java.time.Instant;

public record UserDeletedEvent(
        String userId,
        Instant occurredAt
) implements UserEvent {

    public static UserDeletedEvent of(String userId) {
        return new UserDeletedEvent(userId, Instant.now());
    }

    @Override
    public String type() {
        return "UserDeletedEvent";
    }
}
