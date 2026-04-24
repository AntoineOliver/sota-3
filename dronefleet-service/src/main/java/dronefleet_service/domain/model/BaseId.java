package dronefleet_service.domain.model;

import common.ddd.ValueObject;

import java.util.Objects;

public record BaseId(String id) implements ValueObject {

    public BaseId {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("BaseId cannot be null");

        if (!id.startsWith("BASE_"))
            throw new IllegalArgumentException("BaseId must start with BASE_");
    }

    public static BaseId of(String name) {
        return new BaseId("BASE_" + name.toUpperCase().replace(" ", "_"));
    }
    public static BaseId is(String name) {
        return new BaseId(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseId)) return false;
        BaseId baseId = (BaseId) o;
        return Objects.equals(id, baseId.id);
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
