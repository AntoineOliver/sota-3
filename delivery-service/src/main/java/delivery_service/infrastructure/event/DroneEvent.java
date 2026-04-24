package delivery_service.infrastructure.event;

public record DroneEvent(
        String deliveryId,
        String droneId,
        DroneEventType type,
        Double latitude,
        Double longitude
) {

    public enum DroneEventType {
        DRONE_ASSIGNED,
        DRONE_AT_PICKUP,
        DRONE_DELIVERED,
        DRONE_LEFT_BASE,
        DRONE_LOCATION_UPDATED,
        DRONE_BACKED_TO_BASE
    }

}
