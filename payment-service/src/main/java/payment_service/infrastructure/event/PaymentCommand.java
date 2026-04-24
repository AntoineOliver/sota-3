package payment_service.infrastructure.event;

import java.time.Instant;

public record PaymentCommand(
        String commandId,
        PaymentCommandType type,
        String userId,
        String deliveryId,
        double amount,
        String currency,
        Instant occurredAt
) {
    public enum PaymentCommandType {
        REQUEST_PAYMENT,
        REQUEST_REFUND
    }
}
