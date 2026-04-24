package dronefleet_service.infrastructure.repository;

import common.hexagonal.Adapter;
import dronefleet_service.domain.model.*;
import dronefleet_service.application.port.DroneRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.stream.Collectors;

@Adapter
@Component
public class InMemoryDroneRepository implements DroneRepository {


    private final Map<DroneId, Drone> drones = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);


    @Override
    public void save(Drone drone) {
        drones.put(drone.id(), drone);
    }

    @Override
    public boolean isPresent(DroneId droneId) {
        return drones.containsKey(droneId);
    }

    @Override
    public Optional<Drone> findById(DroneId droneId) {
        return Optional.ofNullable(drones.get(droneId));
    }

    @Override
    public List<Drone> findAll() {
        return new ArrayList<>(drones.values());
    }

    @Override
    public List<Drone> findAllActive() {
        return drones.values().stream()
                .filter(Drone::isInFlight)
                .toList();
    }

    @Override
    public List<Drone> findAvailableByBase(BaseId baseId) {
        return drones.values().stream()
                .filter(d -> d.baseId().equals(baseId))
                .filter(d -> d.state() == DroneState.AVAILABLE)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(DroneId droneId) {
        drones.remove(droneId);
    }

    @Override
    public int nextId() {
        return counter.incrementAndGet();
    }
}
