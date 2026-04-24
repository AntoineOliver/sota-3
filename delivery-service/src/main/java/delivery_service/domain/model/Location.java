package delivery_service.domain.model;

import common.ddd.ValueObject;

public record Location(double latitude, double longitude) implements ValueObject {

    public Location {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }
}
