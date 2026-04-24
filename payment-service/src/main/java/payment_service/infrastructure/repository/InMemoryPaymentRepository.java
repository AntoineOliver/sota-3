package payment_service.infrastructure.repository;

import payment_service.application.port.PaymentRepository;
import payment_service.domain.event.PaymentEvent;
import payment_service.domain.model.Payment;
import payment_service.domain.model.PaymentId;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, List<PaymentEvent>> eventStore = new ConcurrentHashMap<>();

    @Override
    public void append(PaymentEvent event) {
        String paymentId = extractPaymentId(event);
        eventStore.computeIfAbsent(paymentId, ignored -> new ArrayList<>()).add(event);
    }

    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        List<PaymentEvent> events = eventStore.get(paymentId.toString());
        if (events == null || events.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Payment.rehydrate(List.copyOf(events)));
    }

    @Override
    public List<PaymentEvent> findEventsById(PaymentId paymentId) {
        return List.copyOf(eventStore.getOrDefault(paymentId.toString(), List.of()));
    }

    @Override
    public boolean isPresent(PaymentId paymentId) {
        return eventStore.containsKey(paymentId.toString());
    }

    private String extractPaymentId(PaymentEvent event) {
        if (event instanceof payment_service.domain.event.PaymentInitiatedEvent initiatedEvent) {
            return initiatedEvent.paymentId();
        }
        if (event instanceof payment_service.domain.event.PaymentSucceededEvent succeededEvent) {
            return succeededEvent.paymentId();
        }
        if (event instanceof payment_service.domain.event.PaymentFailedEvent failedEvent) {
            return failedEvent.paymentId();
        }
        if (event instanceof payment_service.domain.event.RefundIssuedEvent refundIssuedEvent) {
            return refundIssuedEvent.paymentId();
        }
        if (event instanceof payment_service.domain.event.RefundSucceededEvent refundSucceededEvent) {
            return refundSucceededEvent.paymentId();
        }
        if (event instanceof payment_service.domain.event.RefundFailedEvent refundFailedEvent) {
            return refundFailedEvent.paymentId();
        }
        throw new IllegalArgumentException("Unsupported payment event type: " + event.getClass().getName());
    }
}
