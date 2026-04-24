package payment_service.domain.model;

import common.ddd.Aggregate;
import payment_service.domain.event.*;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Payment implements Aggregate<PaymentId> {

    private PaymentId paymentId;
    private String userId;
    private String deliveryId;
    private Amount amount;
    private Instant whenCreated;

    private PaymentState state;

    private Payment(PaymentId paymentId, String userId, String deliveryId, Amount amount, Instant whenCreated) {
        this.paymentId = Objects.requireNonNull(paymentId);
        this.userId = Objects.requireNonNull(userId);
        this.deliveryId = Objects.requireNonNull(deliveryId);
        this.amount = Objects.requireNonNull(amount);
        this.whenCreated = Objects.requireNonNull(whenCreated);
        this.state = PaymentState.PAYMENT_PENDING;
    }

    private Payment() {
    }

    public static Payment create(PaymentId id, String userId, String deliveryId, Amount amount) {
        return new Payment(id, userId, deliveryId, amount, Instant.now());
    }

    public static Payment rehydrate(List<PaymentEvent> events) {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("Cannot rehydrate payment without events");
        }

        Payment payment = new Payment();
        events.forEach(payment::apply);
        return payment;
    }

    public PaymentId getPaymentId() { return paymentId; }
    public String getUserId() { return userId; }
    public String getDeliveryId() { return deliveryId; }
    public Amount getAmount() { return amount; }
    public Instant getWhenCreated() { return whenCreated; }
    public PaymentState getState() { return state; }

    // Domain actions return domain events (to be published)
    public PaymentInitiatedEvent markPaymentInitiated() {
        if (state != PaymentState.PAYMENT_PENDING) {
            throw new IllegalStateException("Payment cannot be initiated in state: " + state);
        }
        // state stays pending while processing
        return PaymentInitiatedEvent.from(this);
    }

    public PaymentSucceededEvent markPaymentConfirmed() {
        if (state != PaymentState.PAYMENT_PENDING) {
            throw new IllegalStateException("Payment cannot be confirmed in state: " + state);
        }
        this.state = PaymentState.PAYMENT_CONFIRMED;
        return PaymentSucceededEvent.from(this);
    }

    public PaymentFailedEvent markPaymentFailed(String reason) {
        if (state != PaymentState.PAYMENT_PENDING) {
            throw new IllegalStateException("Payment cannot fail in state: " + state);
        }
        this.state = PaymentState.PAYMENT_FAILED;
        return PaymentFailedEvent.from(this, reason);
    }

    public RefundIssuedEvent markRefundIssued() {
        if (state != PaymentState.PAYMENT_CONFIRMED) {
            throw new IllegalStateException("Refund can only be issued for confirmed payments");
        }
        this.state = PaymentState.REFUND_REQUESTED;
        return RefundIssuedEvent.from(this);
    }

    public RefundSucceededEvent markRefundSucceeded() {
        this.state = PaymentState.REFUND_CONFIRMED;
        return RefundSucceededEvent.from(this);
    }

    public RefundFailedEvent markRefundFailed(String reason) {
        this.state = PaymentState.REFUND_FAILED;
        return RefundFailedEvent.from(this, reason);
    }

    @Override
    public PaymentId getId() {
        return paymentId;
    }

    private void apply(PaymentEvent event) {
        if (event instanceof PaymentInitiatedEvent initiatedEvent) {
            if (paymentId == null) {
                this.paymentId = new PaymentId(initiatedEvent.paymentId());
                this.userId = initiatedEvent.userId();
                this.deliveryId = initiatedEvent.deliveryId();
                this.amount = new Amount(initiatedEvent.amount(), initiatedEvent.currency());
                this.whenCreated = initiatedEvent.occurredAt();
            }
            this.state = PaymentState.PAYMENT_PENDING;
            return;
        }

        if (event instanceof PaymentSucceededEvent) {
            this.state = PaymentState.PAYMENT_CONFIRMED;
            return;
        }

        if (event instanceof PaymentFailedEvent) {
            this.state = PaymentState.PAYMENT_FAILED;
            return;
        }

        if (event instanceof RefundIssuedEvent) {
            this.state = PaymentState.REFUND_REQUESTED;
            return;
        }

        if (event instanceof RefundSucceededEvent) {
            this.state = PaymentState.REFUND_CONFIRMED;
            return;
        }

        if (event instanceof RefundFailedEvent) {
            this.state = PaymentState.REFUND_FAILED;
        }
    }

    public enum PaymentState {
        PAYMENT_PENDING,
        PAYMENT_CONFIRMED,
        PAYMENT_FAILED,
        REFUND_REQUESTED,
        REFUND_CONFIRMED,
        REFUND_FAILED,
    }
}
