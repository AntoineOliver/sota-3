package delivery_service.application.dto;

import java.time.Instant;

public record PaymentResponseDTO(
        String paymentId,
        String userId,
        String deliveryId,
        double amount,
        String currency,
        Instant whenCreated,
        String state
) { }
