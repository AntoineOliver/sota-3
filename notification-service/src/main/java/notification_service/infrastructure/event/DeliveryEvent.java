package notification_service.infrastructure.event;

public record DeliveryEvent(
        String deliveryId,
        String userId,
        DeliveryEventType type,
        String reason,
        String eta,
        Integer remainingMinutes
) {

    public enum DeliveryEventType {
        DELIVERY_CREATED,
        DELIVERY_STARTED,
        DELIVERY_COMPLETED,
        DELIVERY_FAILED,
        DELIVERY_CANCELED,
        WAIT_FOR_PICKUP, ETA_UPDATED
    }
}