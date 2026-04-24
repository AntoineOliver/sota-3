package dronefleet_service.domain.event;


import java.time.Instant;

public record DroneMaintenanceScheduledEvent(String droneId, String reason, Instant occurredAt) implements DroneEvent {
    public static DroneMaintenanceScheduledEvent of(String droneId, String reason) {
        return new DroneMaintenanceScheduledEvent(droneId, reason, Instant.now());
    }


    @Override public String type() { return "DroneMaintenanceScheduledEvent"; }
}