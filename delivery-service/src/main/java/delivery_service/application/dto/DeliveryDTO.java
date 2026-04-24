package delivery_service.application.dto;

import java.time.Instant;

public record DeliveryDTO(
        String deliveryId,
        double pickupLat,
        double pickupLon,
        double dropoffLat,
        double dropoffLon,
        double weight,
        double amount,
        String currency,
        Instant eta
) { }
