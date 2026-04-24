package payment_service.domain.event;

import payment_service.domain.model.Payment;

import java.time.Instant;

public record PaymentFailedEvent(String paymentId, String userId, String deliveryId, double amount, String currency, String reason, Instant occurredAt)
        implements PaymentEvent {

    public static PaymentFailedEvent from(Payment p, String reason) {
        return new PaymentFailedEvent(p.getPaymentId().toString(), p.getUserId(), p.getDeliveryId(),
                p.getAmount().amount().doubleValue(), p.getAmount().currency().toString(), reason, Instant.now());
    }

    @Override public String type() { return "PaymentFailedEvent"; }
}
