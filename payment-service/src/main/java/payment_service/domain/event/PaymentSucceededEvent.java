package payment_service.domain.event;

import payment_service.domain.model.Payment;

import java.time.Instant;

public record PaymentSucceededEvent(String paymentId, String userId, String deliveryId, double amount, String currency, Instant occurredAt)
        implements PaymentEvent {

    public static PaymentSucceededEvent from(Payment p) {
        return new PaymentSucceededEvent(p.getPaymentId().toString(), p.getUserId(), p.getDeliveryId(),
                p.getAmount().amount().doubleValue(), p.getAmount().currency().toString(), Instant.now());
    }

    @Override public String type() { return "PaymentSucceededEvent"; }
}
