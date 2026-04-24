package user_service.application.dto;

public record ChangePasswordRequestDTO(
        String userId,
        String oldPassword,
        String newPassword
) {
}
