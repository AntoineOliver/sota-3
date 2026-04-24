package dronefleet_service.domain.event;


import java.time.Instant;

public record DroneLeftBaseEvent(String droneId, String baseId, Instant occurredAt) implements DroneEvent {
    public static DroneLeftBaseEvent of(String droneId, String baseId) {
        return new DroneLeftBaseEvent(droneId, baseId, Instant.now());
    }

    @Override public String type() { return "DroneLeftBaseEvent"; }
}