package user_service.application.dto;

public record EmailUpdatedResponseDTO(
        String id,
        String email,
        String message
) {
}
