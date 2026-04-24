package notification_service.infrastructure.event;

public record UserEvent(
        String userId,
        String username,
        String email,
        UserEventType type,
        String oldEmail,
        String newEmail
) {

    public enum UserEventType {
        USER_REGISTERED,
        USER_DELETED, USER_EMAIL_UPDATED
    }
}