package delivery_service.domain.event;

import java.time.Instant;

public record WaitForPickUpEvent(
        String deliveryId,
        String userId,
        Instant occurredAt
) implements DeliveryEvent {

    public static WaitForPickUpEvent of(String deliveryId, String userId) {
        return new WaitForPickUpEvent(deliveryId, userId, Instant.now());
    }

    @Override
    public String type() {
        return "WaitForPickUpEvent";
    }
}