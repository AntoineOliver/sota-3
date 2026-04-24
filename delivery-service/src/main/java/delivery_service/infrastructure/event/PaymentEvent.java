package delivery_service.infrastructure.event;

public record PaymentEvent(
        String deliveryId,
        PaymentEventType type
){
    public enum PaymentEventType {
        PAYMENT_SUCCEEDED,
        PAYMENT_FAILED,
        REFUND_SUCCEEDED,
        REFUND_FAILED
    }
}


