package dronefleet_service.application.dto;

public record BaseInfoDTO(
    String base,
    double latitude,
    double longitude,
    double numberDrones,
    double capacity
) {
}
