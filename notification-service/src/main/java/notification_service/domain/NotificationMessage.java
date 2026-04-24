package notification_service.domain;

import java.time.Instant;

public record NotificationMessage(
        String userId,
        String subject,
        String body,
        Instant occurredAt
) {}
