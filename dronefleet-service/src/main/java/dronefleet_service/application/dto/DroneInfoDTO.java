package dronefleet_service.application.dto;

public record DroneInfoDTO(
        String id,
        String baseId,
        double battery,
        Double lat,
        Double lon,
        double km,
        String state,
        String currentDeliveryId,
        Double dropoffLocationLat,
        Double dropoffLocationLon,
        boolean enRouteToPickUp
) {
}
