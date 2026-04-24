package payment_service.infrastructure.repository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import payment_service.domain.event.PaymentEvent;
import payment_service.domain.model.Amount;
import payment_service.domain.model.Payment;
import payment_service.domain.model.PaymentId;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class InMemoryPaymentRepositoryIntegrationTest {

    private final InMemoryPaymentRepository repository = new InMemoryPaymentRepository();

    @Test
    void findByIdShouldRehydrateAggregateFromStoredEvents() {
        Payment payment = Payment.create(
                PaymentId.from("user-1", "order-0001"),
                "user-1",
                "order-0001",
                new Amount(15.25, "EUR")
        );

        repository.append(payment.markPaymentInitiated());
        repository.append(payment.markPaymentConfirmed());

        Payment rehydrated = repository.findById(PaymentId.from("user-1", "order-0001")).orElseThrow();

        assertEquals(Payment.PaymentState.PAYMENT_CONFIRMED, rehydrated.getState());
        assertEquals("user-1", rehydrated.getUserId());
        assertEquals("order-0001", rehydrated.getDeliveryId());
    }

    @Test
    void findEventsByIdShouldReturnFullOrderedEventStream() {
        Payment payment = Payment.create(
                PaymentId.from("user-2", "order-0002"),
                "user-2",
                "order-0002",
                new Amount(28.00, "EUR")
        );

        PaymentEvent initiated = payment.markPaymentInitiated();
        PaymentEvent failed = payment.markPaymentFailed("card declined");

        repository.append(initiated);
        repository.append(failed);

        List<PaymentEvent> events = repository.findEventsById(PaymentId.from("user-2", "order-0002"));

        assertEquals(2, events.size());
        assertEquals(initiated, events.get(0));
        assertEquals(failed, events.get(1));
        assertTrue(repository.isPresent(PaymentId.from("user-2", "order-0002")));
    }
}
