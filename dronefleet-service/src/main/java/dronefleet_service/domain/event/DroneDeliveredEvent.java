package dronefleet_service.domain.event;


import java.time.Instant;

public record DroneDeliveredEvent(String deliveryId, String droneId, Instant occurredAt) implements DroneEvent {
    public static DroneDeliveredEvent of(String deliveryId, String droneId) { return new DroneDeliveredEvent(deliveryId, droneId, Instant.now()); }


    @Override public String type() { return "DroneDeliveredEvent"; }
}

