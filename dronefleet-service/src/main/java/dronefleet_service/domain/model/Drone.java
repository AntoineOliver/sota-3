package dronefleet_service.domain.model;

import common.ddd.Aggregate;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public class Drone implements Aggregate<DroneId> {


    /* =========================
       === ENUMS INTERNES ======
       ========================= */

    public enum MissionPhase {
        NONE,
        TO_PICKUP,
        TO_DROPOFF,
        RETURNING
    }

    /* =========================
       === ATTRIBUTS ===========
       ========================= */

    private final DroneId id;
    private BaseId baseId;
    private Battery battery;
    private Location location;
    private DroneState state;
    private double kilometrage;

    // mission
    private String currentDeliveryId;
    private Location pickupLocation;
    private Location dropoffLocation;
    private Location returnBaseLocation;
    private MissionPhase missionPhase;


    /* =========================
       === CONSTRUCTEUR =========
       ========================= */

    public Drone(DroneId id,
                 BaseId baseId,
                 Battery battery,
                 Location location,
                 Double kilometrage) {

        this.id = Objects.requireNonNull(id);
        this.baseId = Objects.requireNonNull(baseId);
        this.battery = Objects.requireNonNull(battery);
        this.location = Objects.requireNonNull(location);

        this.kilometrage = kilometrage == null ? 0.0 : kilometrage;

        this.state = DroneState.AVAILABLE;
        this.missionPhase = MissionPhase.NONE;

        this.currentDeliveryId = null;
        this.pickupLocation = null;
        this.dropoffLocation = null;
        this.returnBaseLocation = null;

    }

    /* =========================
       === GETTERS =============
       ========================= */

    public DroneId id() { return id; }
    public BaseId baseId() { return baseId; }
    public Battery battery() { return battery; }
    public Location location() { return location; }
    public DroneState state() { return state; }
    public double kilometrage() { return kilometrage; }

    public Optional<String> currentDeliveryId() {
        return Optional.ofNullable(currentDeliveryId);
    }

    public Optional<Location> pickupLocation() {
        return Optional.ofNullable(pickupLocation);
    }

    public Optional<Location> dropoffLocation() {
        return Optional.ofNullable(dropoffLocation);
    }

    public Optional<Location> returnBaseLocation() {
        return Optional.ofNullable(returnBaseLocation);
    }

    /* =========================
       === ÉTAT & MISSION ======
       ========================= */

    public boolean isInFlight() {
        return missionPhase != MissionPhase.NONE;
    }

    public boolean isEnRouteToPickup() {
        return missionPhase == MissionPhase.TO_PICKUP;
    }

    public boolean isEnRouteToDropoff() {
        return missionPhase == MissionPhase.TO_DROPOFF;
    }

    public boolean isReturningToBase() {
        return missionPhase == MissionPhase.RETURNING;
    }

    /* =========================
       === COMMANDES MÉTIER ====
       ========================= */

    /** Assignation initiale */
    public void assignToDelivery(String deliveryId, Location pickup, Location dropoff) {
        if (state != DroneState.AVAILABLE)
            throw new IllegalStateException("Drone not available");

        this.currentDeliveryId = deliveryId;
        this.pickupLocation = pickup;
        this.dropoffLocation = dropoff;
        this.missionPhase = MissionPhase.TO_PICKUP;
        this.state = DroneState.BUSY;
    }

    /** Avancement physique */
    public void updateLocation(Location newLocation, double kmDelta, Battery newBattery) {
        this.location = newLocation;
        this.kilometrage += kmDelta;
        this.battery = newBattery;
    }


    /** Pickup atteint */
    public void markAtPickup() {
        if (missionPhase != MissionPhase.TO_PICKUP)
            throw new IllegalStateException("Not en route to pickup");

        this.missionPhase = MissionPhase.TO_DROPOFF;
    }

    /** Livraison terminée → retour base */
    public void startReturnToBase(Location baseLocation) {
        if (missionPhase != MissionPhase.TO_DROPOFF)
            throw new IllegalStateException("Cannot return, delivery not completed");

        this.returnBaseLocation = baseLocation;
        this.missionPhase = MissionPhase.RETURNING;
        this.state = DroneState.RETURNING;
    }

    public void arriveAtBase() {
        this.state = DroneState.AVAILABLE;

        this.missionPhase = MissionPhase.NONE;

        this.currentDeliveryId = null;
        this.pickupLocation = null;
        this.dropoffLocation = null;
        this.returnBaseLocation = null;

        this.battery = battery.recharge();
    }

    /* =========================
       === DDD =================
       ========================= */

    @Override
    public DroneId getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Drone{" +
                "id=" + id +
                ", state=" + state +
                ", mission=" + missionPhase +
                ", location=" + location +
                ", battery=" + battery.percent() + "%" +
                '}';
    }
}
