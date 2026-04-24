package dronefleet_service.application.dto;

public record BaseCreatedResponseDTO(
        String name,
        double latitude,
        double longitude,
        double capacity
) {
}
