package payment_service.domain.event;

import payment_service.domain.model.Payment;

import java.time.Instant;

public record RefundFailedEvent(String paymentId, String userId, String deliveryId, double amount, String currency, String reason, Instant occurredAt)
        implements PaymentEvent {

    public static RefundFailedEvent from(Payment p, String reason) {
        return new RefundFailedEvent(p.getPaymentId().toString(), p.getUserId(), p.getDeliveryId(),
                p.getAmount().amount().doubleValue(), p.getAmount().currency().toString(), reason, Instant.now());
    }

    @Override public String type() { return "RefundFailedEvent"; }
}
