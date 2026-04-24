package dronefleet_service.domain.event;

import java.time.Instant;

public record DroneReturnedToBaseEvent(String droneId, Instant occurredAt) implements DroneEvent {
    public static DroneReturnedToBaseEvent of(String droneId) {
        return new DroneReturnedToBaseEvent(droneId, Instant.now());
    }


    @Override public String type() { return "DroneAssignedEvent"; }
}
