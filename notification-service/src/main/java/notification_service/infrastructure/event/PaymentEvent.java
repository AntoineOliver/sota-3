package notification_service.infrastructure.event;

public record PaymentEvent(
        String deliveryId,
        String userId,
        PaymentEventType type,
        String amount,
        String currency,
        String reason
) {

    public enum PaymentEventType {
        PAYMENT_SUCCEEDED,
        PAYMENT_FAILED,
        REFUND_SUCCEEDED,
        REFUND_FAILED
    }
}