package delivery_service.domain.event;

import java.time.Instant;

public record DeliveryFailedEvent(
        String deliveryId,
        String userId,
        String reason,
        Instant occurredAt
) implements DeliveryEvent {

    public static DeliveryFailedEvent of(String deliveryId, String userId, String reason) {
        return new DeliveryFailedEvent(deliveryId, userId, reason, Instant.now());
    }

    @Override
    public String type() {
        return "DeliveryFailedEvent";
    }
}