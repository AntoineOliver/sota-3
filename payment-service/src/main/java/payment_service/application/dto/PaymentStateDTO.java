package payment_service.application.dto;

public enum PaymentStateDTO {
    PAYMENT_PENDING,
    PAYMENT_CONFIRMED,
    PAYMENT_FAILED,
    REFUND_REQUESTED,
    REFUND_CONFIRMED,
    REFUND_FAILED
}
