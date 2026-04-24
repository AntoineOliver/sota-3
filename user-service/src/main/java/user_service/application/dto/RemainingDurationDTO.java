package user_service.application.dto;

import java.time.Instant;

public record RemainingDurationDTO(
        String deliveryId,
        long remainingMinutes
) { }