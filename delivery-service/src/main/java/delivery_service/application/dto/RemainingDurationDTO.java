package delivery_service.application.dto;

public record RemainingDurationDTO(
        String deliveryId,
        long remainingMinutes,
        double lat,
        double lon
) { }
