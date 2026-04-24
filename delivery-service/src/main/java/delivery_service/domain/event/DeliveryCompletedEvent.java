package delivery_service.domain.event;

import java.time.Instant;

public record DeliveryCompletedEvent(
        String deliveryId,
        String userId,
        Instant occurredAt
) implements DeliveryEvent {

    public static DeliveryCompletedEvent of(String deliveryId, String userId) {
        return new DeliveryCompletedEvent(deliveryId, userId, Instant.now());
    }

    @Override
    public String type() {
        return "DeliveryCompletedEvent";
    }
}