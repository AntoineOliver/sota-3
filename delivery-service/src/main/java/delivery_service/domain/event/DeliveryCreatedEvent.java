package delivery_service.domain.event;

import java.time.Instant;

/**
 *
 * Domain event: delivery created
 *
 */
public record DeliveryCreatedEvent (String deliveryId, String userId, Instant now) implements DeliveryEvent {

    public static DeliveryCreatedEvent of(String deliveryId, String userId) {
        return new DeliveryCreatedEvent(deliveryId, userId, Instant.now());
    }
    @Override
    public String type() {
        return "DeliveryCreatedEvent";
    }

    @Override
    public Instant occurredAt() {
        return Instant.now();
    }
}