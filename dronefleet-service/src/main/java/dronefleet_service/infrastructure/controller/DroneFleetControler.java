package dronefleet_service.infrastructure.controller;

import dronefleet_service.application.dto.*;
import dronefleet_service.application.port.DroneService;
import dronefleet_service.domain.model.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/drones")
public class DroneFleetControler {

    private final DroneService droneService;
    private static final Logger log = LoggerFactory.getLogger(DroneFleetControler.class);


    public DroneFleetControler(DroneService droneService) {
        this.droneService = droneService;
    }


    @PostMapping("/base")
    public ResponseEntity<BaseCreatedResponseDTO> createBase(@RequestParam String name,
                                           @RequestParam Double latitude,
                                           @RequestParam Double longitude,
                                           @RequestParam int capacity) {
        Base base = droneService.createBase(name,new Location(latitude,longitude),capacity);
        return ResponseEntity.ok(
                new BaseCreatedResponseDTO(
                        base.id().id(),
                        latitude,
                        longitude,
                        capacity
                )
        );
    }

    @PostMapping("/drone")
    public ResponseEntity<DroneCreatedResponseDTO> createDrone(@RequestParam String baseId) {
        Drone drone = droneService.createDrone(BaseId.is(baseId));
        return ResponseEntity.ok(
                new DroneCreatedResponseDTO(
                        drone.id().id(),
                        baseId
                )
        );
    }

    @PostMapping("/assign")
    public ResponseEntity<DroneAssignmentResponseDTO> assignDrone(@RequestBody DroneAssignmentRequestDTO request) {
        log.info("[DRONE][ASSIGN] assigning drone for delivery id={}", request.deliveryId());
        try {
            droneService.onDroneRequested(request.deliveryId(),new Location(request.pickupLat(),
                    request.pickupLng()),new Location(request.dropoffLat(), request.dropoffLng()));

            DroneId assignedDroneId = droneService.getAssignedDroneId(request.deliveryId())
                    .orElseThrow(() -> new RuntimeException("Drone was not assigned"));

            log.info("[DRONE][ASSIGN] drone found and assigned for delivery id={}, drone Id = {}", request.deliveryId(), assignedDroneId);

            return ResponseEntity.ok(new DroneAssignmentResponseDTO(assignedDroneId.id()));

        } catch (Exception e) {
            return ResponseEntity.status(400).body(new DroneAssignmentResponseDTO(null, e.getMessage()));
        }
    }

    @GetMapping("/base")
    public ResponseEntity<BaseInfoDTO> getBaseInfo(@RequestParam String baseId) {
        log.info("[DRONE][BASE] getting info for base with id={}",baseId);
        Base b;
        b = droneService.getBase(BaseId.is(baseId));
        log.info("[DRONE][BASE] base got with id={}",baseId);
        return ResponseEntity.ok(
                new BaseInfoDTO(
                        baseId,b.location().lat(),b.location().lon(),
                        droneService.getNumberOfDrones(BaseId.is(baseId)),
                        b.capacity()
                )
        );
    }

    @GetMapping("/drone")
    public ResponseEntity<DroneInfoDTO> getDroneInfo(@RequestParam String droneId) {
        log.info("[DRONE][DRONE] getting info for drone with id={}",droneId);
        Drone d;
        d = droneService.getDrone(DroneId.is(droneId));
        log.info("[DRONE][DRONE] drone found with id={}",droneId);
        Location pickup = d.pickupLocation().orElse(null);
        Location dropoff = d.dropoffLocation().orElse(null);

        return ResponseEntity.ok(
                new DroneInfoDTO(
                        d.id().id(),
                        d.baseId().id(),
                        d.battery().percent(),
                        pickup != null ? pickup.lat() : null,
                        pickup != null ? pickup.lon() : null,
                        d.kilometrage(),
                        d.state().name(),
                        d.currentDeliveryId().orElse(null),
                        dropoff != null ? dropoff.lat() : null,
                        dropoff != null ? dropoff.lon() : null,
                        d.isEnRouteToPickup()
                )
        );
    }
}
