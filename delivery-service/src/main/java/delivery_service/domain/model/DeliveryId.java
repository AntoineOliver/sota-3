package delivery_service.domain.model;

import common.ddd.ValueObject;

import java.util.concurrent.atomic.AtomicInteger;

public record DeliveryId(String id) implements ValueObject {


    public DeliveryId {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("DeliveryId cannot be null or blank");
        }
        if (!id.matches("^order-\\d{4}$")) {
            throw new IllegalArgumentException("Invalid DeliveryId format (expected order-XXXX)");
        }
    }

    public static DeliveryId of(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("DeliveryId input cannot be null or blank");
        }

        // Cas 1 : déjà au format "order-XXXX"
        if (input.startsWith("order-")) {
            return new DeliveryId(input);
        }

        // Cas 2 : entrée numérique → on concatène
        if (input.matches("\\d+")) {
            return new DeliveryId("order-" + input);
        }

        throw new IllegalArgumentException(
                "Invalid DeliveryId input (must be digits or start with 'order-')"
        );
    }

    @Override
    public String toString() {
        return id;
    }
}
