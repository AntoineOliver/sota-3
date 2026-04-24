package dronefleet_service.application.dto;

public record DroneAssignmentRequestDTO(
        String deliveryId,
        double pickupLat,
        double pickupLng,
        double dropoffLat,
        double dropoffLng,
        double weightKg,
        String userId
) { }
