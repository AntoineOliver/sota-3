package user_service.application.dto;

public record PasswordChangedResponseDTO(
        String userId,
        String message
) {
}
