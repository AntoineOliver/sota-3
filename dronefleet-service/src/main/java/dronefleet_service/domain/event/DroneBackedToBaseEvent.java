package dronefleet_service.domain.event;

import dronefleet_service.domain.event.DroneEvent;

import java.time.Instant;

public record DroneBackedToBaseEvent(String droneId, String baseId, Instant occurredAt) implements DroneEvent {
    public static DroneBackedToBaseEvent of(String droneId, String baseId) {
        return new DroneBackedToBaseEvent(droneId, baseId, Instant.now());
    }

    @Override public String type() { return "DroneBackedToBaseEvent"; }
}