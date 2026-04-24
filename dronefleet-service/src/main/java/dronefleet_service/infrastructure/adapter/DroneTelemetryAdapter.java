package dronefleet_service.infrastructure.adapter;

import common.hexagonal.Adapter;
import dronefleet_service.application.port.DroneRepository;
import dronefleet_service.application.port.DroneService;
import dronefleet_service.application.port.DroneTelemetryPort;
import dronefleet_service.application.port.EventPublisherPort;
import dronefleet_service.domain.model.Drone;
import dronefleet_service.domain.model.Location;
import dronefleet_service.domain.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Adapter
@Component
public class DroneTelemetryAdapter implements DroneTelemetryPort {

    private static final Logger log = LoggerFactory.getLogger(DroneTelemetryAdapter.class);

    private static final double DRONE_SPEED_KMPH = 15.0;
    private static final double ARRIVAL_THRESHOLD_KM = 0.03; // 30 m

    private final DroneRepository droneRepository;
    private final DroneService service;

    public DroneTelemetryAdapter(
            DroneRepository droneRepository,
            DroneService service
    ) {
        this.droneRepository = droneRepository;
        this.service = service;
    }

    @Override
    public void simulateStep(int minutesSimulated) {
        double stepKm = DRONE_SPEED_KMPH * minutesSimulated / 60.0;

        List<Drone> drones = droneRepository.findAllActive();

        for (Drone drone : drones) {
            if (!drone.isInFlight()) continue;

            Optional<Location> targetOpt = resolveTarget(drone);
            if (targetOpt.isEmpty()) continue;

            Location current = drone.location();
            Location target = targetOpt.get();

            double remainingKm = current.distanceTo(target);
            double travelledKm = Math.min(stepKm, remainingKm);

            Location newLocation = current.moveTowards(target, travelledKm);
            service.onDroneMoved(drone.id(),newLocation.lat(),newLocation.lon(),travelledKm);

            if (newLocation.distanceTo(target) <= ARRIVAL_THRESHOLD_KM) {
                publishArrivalEvent(drone);
            }
        }
    }

    private Optional<Location> resolveTarget(Drone drone) {
        if (drone.isEnRouteToPickup()) return drone.pickupLocation();
        if (drone.isEnRouteToDropoff()) return drone.dropoffLocation();
        if (drone.isReturningToBase()) return drone.returnBaseLocation();
        return Optional.empty();
    }

    private void publishArrivalEvent(Drone drone) {
        if (drone.isEnRouteToPickup()) {
            log.info("[TELEMETRY] Drone {} reached PICKUP", drone.id());
            Optional<String> deliveryIdOpt = drone.currentDeliveryId();
            deliveryIdOpt.ifPresent(s -> service.onDroneArrivedAtPickup(drone.id(), s));
        } else if (drone.isEnRouteToDropoff()) {
            log.info("[TELEMETRY] Drone {} reached DROPOFF", drone.id());
            Optional<String> deliveryIdOpt = drone.currentDeliveryId();
            deliveryIdOpt.ifPresent(s -> service.onDroneArrivedAtDropoff(drone.id(), s));
        } else if (drone.isReturningToBase()) {
            log.info("[TELEMETRY] Drone {} reached BASE", drone.id());
            service.onDroneArrivedAtBase(drone.id());
        }
    }

}
