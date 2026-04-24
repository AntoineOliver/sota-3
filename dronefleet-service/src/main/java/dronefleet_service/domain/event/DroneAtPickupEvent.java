package dronefleet_service.domain.event;

import java.time.Instant;

public record DroneAtPickupEvent(String deliveryId, String droneId, Instant occurredAt) implements DroneEvent {
    public static DroneAtPickupEvent of(String deliveryId, String droneId) { return new DroneAtPickupEvent(deliveryId, droneId, Instant.now()); }

    @Override public String type() { return "DroneAtPickupEvent"; }
}