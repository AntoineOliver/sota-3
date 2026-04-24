package dronefleet_service.application.service;

import dronefleet_service.application.exception.BaseAlreadyExistsException;
import dronefleet_service.application.exception.BaseNotFoundException;
import dronefleet_service.application.exception.DroneAlreadyExistsException;
import dronefleet_service.application.exception.DroneNotFoundException;
import dronefleet_service.application.port.*;
import dronefleet_service.domain.event.*;
import dronefleet_service.domain.model.*;
import dronefleet_service.domain.service.DroneCalculator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneServiceImpl implements DroneService {

    private final DroneRepository droneRepository;
    private final BaseRepository baseRepository;
    private final DeliveryServicePort deliveryPort;
    private final EventPublisherPort eventPublisher;
    private final DroneCalculator calculator;
    private final double maintenanceKmThreshold;
    private static final double BATTERY_PERCENT_PER_KM = 2.0;
    private static final Logger log = LoggerFactory.getLogger(DroneServiceImpl.class);


    public DroneServiceImpl(DroneRepository droneRepository,
                            BaseRepository baseRepository,
                            DeliveryServicePort deliveryPort,
                            EventPublisherPort eventPublisher,
                            DroneCalculator calculator,
                            double maintenanceKmThreshold) {
        this.droneRepository = droneRepository;
        this.baseRepository = baseRepository;
        this.deliveryPort = deliveryPort;
        this.eventPublisher = eventPublisher;
        this.calculator = calculator;
        this.maintenanceKmThreshold = maintenanceKmThreshold;
    }

    @Override
    public Base createBase(String name,Location baseLocation, int capacity) {
        log.info("[BASE][CREATE] creating location lat={} and long={}", baseLocation.lat(),baseLocation.lon());

        BaseId id = BaseId.of(name);
        log.info("[BASE][CREATE] baseId created ={}", id);
        if (baseRepository.isPresent(id) || baseRepository.isPresentByLoc(baseLocation))
            throw new BaseAlreadyExistsException("Already one with this id (name) or this location");

        Base base = new Base(id, baseLocation, capacity);
        log.info("[BASE][CREATE] base created, id={}, lat={} and long={}, capacity = {}",id, baseLocation.lat(),baseLocation.lon(), capacity);
        baseRepository.save(base);
        log.info("[BASE][CREATE] repo updated ={}", base.getId());
        return base;
    }

    @Override
    public Drone createDrone(BaseId baseId) {
        log.info("[DRONE][CREATE] creating drone in base id={}", baseId);

        Base base = baseRepository.findById(baseId)
                .orElseThrow(() -> new BaseNotFoundException("Can't find base"));

        DroneId droneId;
        int attempts = 0;
        do {
            droneId = DroneId.of(droneRepository.nextId());
            attempts++;
            if (attempts > 100) throw new DroneAlreadyExistsException("no more drone possible");
        } while (droneRepository.isPresent(droneId));

        log.info("[DRONE][CREATE] drone Id created id={}", droneId);

        Drone drone = new Drone(droneId,baseId,new Battery(100),base.location(),0.0);
        log.info("[DRONE][CREATE] drone created id={}, baseId ={}", droneId, baseId);
        droneRepository.save(drone);
        log.info("[DRONE][CREATE] repo updated id={}", droneId);
        return drone;
    }

    @Override
    public void onDroneRequested(String deliveryId, Location pickup, Location dropoff) throws Exception {
        log.info("[DRONE][ASSIGN] researching free drone for delivery id={}", deliveryId);

        if (!deliveryPort.deliveryExists(deliveryId)) {
            eventPublisher.publish(
                    DroneCannotBeAssignedEvent.of(deliveryId, "Delivery not found")
            );
            log.info("[DRONE][ASSIGN] delivery not found, event published");
            return;
        }


        Optional<Base> baseOpt = baseRepository.findClosestBase(pickup);

        if (baseOpt.isEmpty()) {
            eventPublisher.publish(DroneCannotBeAssignedEvent.of(deliveryId, "No base found"));
            return;
        }
        Base base = baseOpt.get();

        log.info("[DRONE][ASSIGN] closest base found, id={} for delivery id={}", base.id(),deliveryId);

        List<Drone> candidates = droneRepository.findAvailableByBase(base.id());

        if (candidates.isEmpty()) {
            eventPublisher.publish(DroneCannotBeAssignedEvent.of(deliveryId, "No available drones in base " + base.id()));
            return;
        }
        log.info("[DRONE][ASSIGN] at least one drone found in base id={} for delivery id={}", base.id(), deliveryId);

        Optional<Drone> chosen = candidates.stream()
                .filter(d -> calculator.canPerformMission(d, pickup, dropoff, base.location()))
                .sorted(calculator.bestDroneComparator(pickup))
                .findFirst();

        log.info("[DRONE][ASSIGN] potential drone with enough ressources to do it in base id={} for delivery id={}", base.id(), deliveryId);

        if (chosen.isEmpty()) {
            eventPublisher.publish(DroneCannotBeAssignedEvent.of(deliveryId, "No drone with enough battery"));
            return;
        }

        Drone drone = chosen.get();
        log.info("[DRONE][ASSIGN] drone chosen, drone id = {} base id={} for delivery id={}",drone.id(), base.id(), deliveryId);

        // assign drone with mission coordinates
        drone.assignToDelivery(deliveryId, pickup, dropoff);
        log.info("[DRONE][ASSIGN] drone assigned id = {} base id={} for delivery id={}",drone.id(), base.id(), deliveryId);

        // domain update: drone leaves base
        base.unregisterDrone(drone.id());
        log.info("[DRONE][ASSIGN] drone id = {} unregister from base id={} to do delivery id={}",drone.id(), base.id(), deliveryId);

        droneRepository.save(drone);
        log.info("[DRONE][ASSIGN] drone repo updated id = {} ",drone.id());

        baseRepository.save(base);
        log.info("[DRONE][ASSIGN] base repo updated id={}",base.id());

        // inform delivery synchronously (droneId as String)
        try {
            deliveryPort.assignDroneToDelivery(deliveryId, drone.id().id());
        } catch (Exception e) {
            log.error("[DRONE][ASSIGN] failed to notify delivery, will rely on event", e);
        }
        log.info("[DRONE][ASSIGN] drone id={} assigned request to delivery ={}",drone.id(), deliveryId);

        // publish events async
        eventPublisher.publish(DroneAssignedEvent.of(drone.id().toString(), deliveryId));
        log.info("[DRONE][ASSIGN] drone assigned event published, drone id = {}",drone.id());

        eventPublisher.publish(DroneLeftBaseEvent.of(drone.id().toString(), base.id().toString()));
        log.info("[DRONE][ASSIGN] drone left base event published, drone id = {}, baseid = {}",drone.id(), base.id());
    }

    // === TELEMETRY EVENTS HANDLING ===

    @Override
    public void onDroneArrivedAtPickup(DroneId droneId, String deliveryId) {
        Drone drone = getDrone(droneId);

        log.info("[DRONE SERVICE] Drone {} arrived at PICKUP", droneId);

        drone.markAtPickup();
        droneRepository.save(drone);

        eventPublisher.publish(
                new DroneAtPickupEvent(deliveryId,droneId.id(), Instant.now())
        );
    }

    @Override
    public void onDroneArrivedAtDropoff(DroneId droneId, String deliveryId) {
        Drone drone = getDrone(droneId);

        log.info("[DRONE SERVICE] Drone {} delivered package", droneId);

        eventPublisher.publish(
                new DroneDeliveredEvent(deliveryId, droneId.id(),Instant.now())
        );

        Base base = getBase(drone.baseId());
        log.info("[DRONE SERVICE] drone = {} before return to base", drone.toString());
        drone.startReturnToBase(base.location());

        droneRepository.save(drone);
    }

    @Override
    public void onDroneArrivedAtBase(DroneId droneId) {
        Drone drone = getDrone(droneId);
        Base base = getBase(drone.baseId());

        log.info("[DRONE SERVICE] Drone {} arrived back at BASE {}",
                droneId, drone.baseId());

        drone.arriveAtBase();
        base.registerDrone(drone.id());
        droneRepository.save(drone);

    }

    @Override
    public void onDroneMoved(DroneId droneId, double lat, double lon, double travelledKm) {
        Drone drone = getDrone(droneId);

        Battery updatedBattery = drone.battery().consumeForKm(travelledKm, BATTERY_PERCENT_PER_KM);

        drone.updateLocation(
                new Location(lat, lon),
                travelledKm,
                updatedBattery
        );

        droneRepository.save(drone);

        drone.currentDeliveryId().ifPresent(deliveryId -> {
            DroneLocationUpdatedEvent event = DroneLocationUpdatedEvent.of(
                    deliveryId,
                    droneId.id(),
                    drone.location().lat(),
                    drone.location().lon()
            );
            log.info("[DRONE SERVICE] Publishing DroneLocationUpdatedEvent: {}", event);
            eventPublisher.publish(event);
        });
    }


    @Override
    public Base getBase(BaseId baseId) throws BaseNotFoundException {
        return baseRepository.findById(baseId)
                .orElseThrow(() -> new BaseNotFoundException("base not found"));
    }

    @Override
    public Drone getDrone(DroneId droneId) throws DroneNotFoundException {
        return droneRepository.findById(droneId)
                .orElseThrow(() -> new DroneNotFoundException("drone not found"));
    }

    @Override
    public int getNumberOfDrones(BaseId baseId) throws BaseNotFoundException {
        Base base = getBase(baseId);
        return base.drones().size();
    }

    @Override
    public Optional<DroneId> getAssignedDroneId(String deliveryId) {
        return droneRepository.findAll().stream()
                .filter(d -> d.currentDeliveryId().map(deliveryId::equals).orElse(false))
                .map(Drone::id)
                .findFirst();
    }
}
