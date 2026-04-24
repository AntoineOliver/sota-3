package delivery_service.domain.event;

import java.time.Instant;

public record DeliveryStartedEvent(
        String deliveryId,
        String userId,
        Instant occurredAt
) implements DeliveryEvent {

    public static DeliveryStartedEvent of(String deliveryId, String userId) {
        return new DeliveryStartedEvent(deliveryId, userId, Instant.now());
    }

    @Override
    public String type() {
        return "DeliveryStartedEvent";
    }
}