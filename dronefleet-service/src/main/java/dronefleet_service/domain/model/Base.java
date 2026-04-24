package dronefleet_service.domain.model;

import common.ddd.Aggregate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Base implements Aggregate<BaseId> {

    private final BaseId id;
    private final Location location;
    private final int capacity;
    private final List<DroneId> drones = new ArrayList<>();

    public Base(BaseId id, Location location, int capacity) {
        this.id = Objects.requireNonNull(id);
        this.location = Objects.requireNonNull(location);
        this.capacity = capacity;
    }

    public BaseId id() { return id; }
    public Location location() { return location; }
    public int capacity() { return capacity; }
    public List<DroneId> drones() { return Collections.unmodifiableList(drones); }

    public void registerDrone(DroneId droneId) {
        if (drones.size() >= capacity) throw new IllegalStateException("Base full");
        if (!drones.contains(droneId)) drones.add(droneId);
    }

    public void unregisterDrone(DroneId droneId) {
        drones.remove(droneId);
    }

    public boolean containsDrone(DroneId droneId) {
        return drones.contains(droneId);
    }

    public boolean hasFreeSlot() { return drones.size() < capacity; }

    @Override
    public BaseId getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Base{" + id + " loc=" + location + " cap=" + capacity + " drones=" + drones.size() + "}";
    }
}
