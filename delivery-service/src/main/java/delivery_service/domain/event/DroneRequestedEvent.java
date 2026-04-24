package delivery_service.domain.event;

import java.time.Instant;

/**
 * Drone requested
 */
public class DroneRequestedEvent implements DeliveryEvent {

    @Override
    public String type() {
        return "DroneRequestedEvent";
    }

    @Override
    public Instant occurredAt() {
        return null;
    }
}
