package dronefleet_service.domain.event;

import java.time.Instant;

public record DroneCannotBeAssignedEvent(String deliveryId, String reason, Instant occurredAt) implements DroneEvent {
    public static DroneCannotBeAssignedEvent of(String deliveryId, String reason) {
        return new DroneCannotBeAssignedEvent(deliveryId, reason, Instant.now());
    }


    @Override public String type() { return "DroneCannotBeAssignedEvent"; }
}