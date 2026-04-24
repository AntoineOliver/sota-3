package payment_service.domain.model;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import payment_service.domain.event.PaymentEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Tag("unit")
class PaymentEventSourcingUnitTest {

    @Test
    void markPaymentConfirmedShouldMoveAggregateToConfirmedState() {
        Payment payment = Payment.create(
                PaymentId.from("user-1", "order-0001"),
                "user-1",
                "order-0001",
                new Amount(19.99, "EUR")
        );

        PaymentEvent event = payment.markPaymentConfirmed();

        assertEquals(Payment.PaymentState.PAYMENT_CONFIRMED, payment.getState());
        assertInstanceOf(payment_service.domain.event.PaymentSucceededEvent.class, event);
    }

    @Test
    void rehydrateShouldRestoreRefundConfirmedStateFromEventStream() {
        Payment payment = Payment.create(
                PaymentId.from("user-2", "order-0002"),
                "user-2",
                "order-0002",
                new Amount(42.50, "EUR")
        );

        PaymentEvent initiated = payment.markPaymentInitiated();
        PaymentEvent confirmed = payment.markPaymentConfirmed();
        PaymentEvent refundIssued = payment.markRefundIssued();
        PaymentEvent refundSucceeded = payment.markRefundSucceeded();

        Payment rehydrated = Payment.rehydrate(List.of(initiated, confirmed, refundIssued, refundSucceeded));

        assertEquals(Payment.PaymentState.REFUND_CONFIRMED, rehydrated.getState());
        assertEquals("user-2", rehydrated.getUserId());
        assertEquals("order-0002", rehydrated.getDeliveryId());
        assertEquals(PaymentId.from("user-2", "order-0002"), rehydrated.getPaymentId());
    }
}
