package payment_service.domain.event;

import payment_service.domain.model.Payment;

import java.time.Instant;

public record RefundIssuedEvent(String paymentId, String userId, String deliveryId, double amount, String currency, Instant occurredAt)
        implements PaymentEvent {

    public static RefundIssuedEvent from(Payment p) {
        return new RefundIssuedEvent(p.getPaymentId().toString(), p.getUserId(), p.getDeliveryId(),
                p.getAmount().amount().doubleValue(), p.getAmount().currency().toString(), Instant.now());
    }

    @Override public String type() { return "RefundIssuedEvent"; }
}
