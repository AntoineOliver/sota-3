package dronefleet_service.domain.event;

import dronefleet_service.domain.model.*;

import java.time.Instant;

public record DroneAssignedEvent(String droneId, String deliveryId, Instant occurredAt) implements DroneEvent {
    public static DroneAssignedEvent of(String droneId, String deliveryId) {
        return new DroneAssignedEvent(droneId, deliveryId, Instant.now());
    }

    @Override public String type() { return "DroneAssignedEvent"; }
}