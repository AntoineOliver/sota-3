package payment_service.domain.model;

import common.ddd.ValueObject;

import java.util.Objects;

public record PaymentId(String id) implements ValueObject {

    public PaymentId {
        Objects.requireNonNull(id, "PaymentId cannot be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("PaymentId cannot be blank");
        }
    }

    public static PaymentId from(String userId, String deliveryId) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(deliveryId);
        return new PaymentId(userId + "_" + deliveryId);
    }

    @Override
    public String toString() {
        return id;
    }
}
