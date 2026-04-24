package delivery_service.domain.model;

import common.ddd.ValueObject;

/**
 * @param value en kg
 */
public record Weight(double value) implements ValueObject {

    public Weight {
        if (value <= 0) {
            throw new IllegalArgumentException("Weight must be positive");
        }

        if (value >= 2.3) {
            throw new IllegalArgumentException("Weight must be less than 2.3 kg");
        }
    }
}
