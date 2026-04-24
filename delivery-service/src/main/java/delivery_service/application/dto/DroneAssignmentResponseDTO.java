package delivery_service.application.dto;

public record DroneAssignmentResponseDTO(String droneId, String errorMessage) {
    public DroneAssignmentResponseDTO(String droneId) {
        this(droneId, null);
    }
}
