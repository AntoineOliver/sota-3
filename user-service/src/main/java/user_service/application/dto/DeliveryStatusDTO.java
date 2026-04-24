package user_service.application.dto;

import java.time.Instant;

public record DeliveryStatusDTO(
        String deliveryId,
        String status
) { }