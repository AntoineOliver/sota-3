package dronefleet_service.domain.model;

import common.ddd.ValueObject;

import java.util.Objects;

public record DroneId(String id) implements ValueObject {

    private static final String PREFIX = "D-";

    public DroneId {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("DroneId cannot be null");
        if (!id.startsWith(PREFIX))
            throw new IllegalArgumentException("DroneId must start with " + PREFIX);
    }

    public static DroneId of(int number) {
        return new DroneId(PREFIX + String.format("%05d", number));
    }
    public static DroneId is(String id){return new DroneId(id);}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DroneId)) return false;
        DroneId droneId = (DroneId) o;
        return Objects.equals(id, droneId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
