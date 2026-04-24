package delivery_service.domain.event;

import java.time.Instant;

public record DeliveryCanceledEvent(
        String deliveryId,
        String userId,
        boolean refundNeeded,
        Instant occurredAt
) implements DeliveryEvent {

    public static DeliveryCanceledEvent of(String deliveryId, String userId, boolean refundNeeded) {
        return new DeliveryCanceledEvent(deliveryId, userId, refundNeeded, Instant.now());
    }

    @Override
    public String type() {
        return "DeliveryCanceledEvent";
    }
}