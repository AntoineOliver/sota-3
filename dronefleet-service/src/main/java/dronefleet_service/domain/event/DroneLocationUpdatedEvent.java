package dronefleet_service.domain.event;


import java.time.Instant;

public record DroneLocationUpdatedEvent(String deliveryId, String droneId, double latitude, double longitude, Instant occurredAt) implements DroneEvent {
    public static DroneLocationUpdatedEvent of(String deliveryId, String droneId, double latitude, double longitude) {
        return new DroneLocationUpdatedEvent(deliveryId, droneId, latitude, longitude, Instant.now());
    }


    @Override public String type() { return "DroneLocationUpdatedEvent"; }
}