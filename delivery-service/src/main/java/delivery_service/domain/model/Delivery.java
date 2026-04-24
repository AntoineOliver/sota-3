package delivery_service.domain.model;

import common.ddd.Aggregate;
import delivery_service.application.exception.*;
import delivery_service.domain.event.*;
import delivery_service.domain.service.DeliveryCalculator;

import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

public class Delivery implements Aggregate<DeliveryId> {

    private static final Logger logger = Logger.getLogger("[Delivery]");

    private final DeliveryId id;
    private final DeliveryRequest request;

    private DeliveryStatus status;
    private Optional<String> droneId;

    private final Price price;
    private ETA eta;
    private RemainingDuration remainingDuration;
    private Location droneLocation;

    //-------------------------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    public Delivery(DeliveryId id, DeliveryRequest request, ETA eta, Price price) {
        this.id = id;
        this.request = request;
        this.price = price;
        this.eta = eta;
        this.remainingDuration = new RemainingDuration(eta);
        this.droneId = Optional.empty();
        this.status = DeliveryStatus.CREATED;
    }

    //-------------------------------------------------------------------------
    // UPDATE
    // -------------------------------------------------------------------------

    public EtaUpdatedEvent updateDroneLocation(Location location) {

        if (droneId.isEmpty()) {
            return null;
        }

        this.droneLocation = location;

       // this.eta = DeliveryCalculator.updateETA(this, this.droneLocation, target);
        this.eta = DeliveryCalculator.computeETA_2(this.getStatus(),this.getRequest().pickupLocation(),this.getRequest().dropoffLocation(),location);
        this.remainingDuration = new RemainingDuration(this.eta);

        return EtaUpdatedEvent.of(id.toString(), request.userId(), eta.toString(), remainingDuration.toMinutes());
    }

    /*
    public EtaUpdatedEvent updateDroneLocation(Location location) {

        if (droneId.isEmpty()) {
            throw new IllegalStateException("Cannot update location: no drone assigned");
        }
        this.droneLocation = location;

        if (this.getRequest().dropoffLocation() != null) {
            this.eta = DeliveryCalculator.updateETA(this.droneLocation, this.getRequest().dropoffLocation());
            this.remainingDuration = new RemainingDuration(this.eta);
        }

        return EtaUpdatedEvent.from(id, request.userId(), eta, remainingDuration);
    }
    */

    //-------------------------------------------------------------------------
    // NEXT STEPS
    // -------------------------------------------------------------------------
    public void markPaymentPending() {
        if (status != DeliveryStatus.CREATED)
            throw new IllegalStateException("Payment can only be pending from CREATED state");
        this.status = DeliveryStatus.PAYMENT_PENDING;
    }

    public void markPaymentConfirmed() {
        if (status != DeliveryStatus.PAYMENT_PENDING)
            throw new IllegalStateException("Payment cannot be confirmed from current state");
        this.status = DeliveryStatus.PAYMENT_CONFIRMED;
    }

    public void requestDrone() {
        if (status != DeliveryStatus.PAYMENT_CONFIRMED)
            throw new IllegalStateException("Cannot request drone before payment confirmation");
        this.status = DeliveryStatus.DRONE_ASSIGNMENT_REQUESTED;
    }

    public WaitForPickUpEvent markWaitForPickUp() {
        if (status != DeliveryStatus.DRONE_ASSIGNMENT_REQUESTED)
            throw new IllegalStateException("Cannot pick up before drone is assigned");
        this.status = DeliveryStatus.WAIT_FOR_PICKUP;
        return new WaitForPickUpEvent(id.toString(), request.userId(), Instant.now());
    }

    public DeliveryStartedEvent markPickedUp() {
        if (status != DeliveryStatus.WAIT_FOR_PICKUP)
            throw new IllegalStateException("Cannot pick up before drone is assigned");
        this.status = DeliveryStatus.IN_PROGRESS;
        return new DeliveryStartedEvent(id.toString(), request.userId(), Instant.now());
    }

    public DeliveryCompletedEvent markDelivered() {
        if (status != DeliveryStatus.IN_PROGRESS)
            throw new IllegalStateException("Delivery cannot be completed now");
        this.status = DeliveryStatus.COMPLETED;
        return new DeliveryCompletedEvent(id.toString(), request.userId(), Instant.now());
    }

    public DeliveryCanceledEvent cancel() throws DeliveryAlreadyCompletedException, DeliveryAlreadyStartedException {
        if (this.status == DeliveryStatus.COMPLETED)
            throw new DeliveryAlreadyCompletedException(id);
        if (this.status == DeliveryStatus.IN_PROGRESS)
            throw new DeliveryAlreadyStartedException(id);

        boolean refundNeeded = (status == DeliveryStatus.PAYMENT_CONFIRMED ||
                status == DeliveryStatus.DRONE_ASSIGNMENT_REQUESTED ||
                status == DeliveryStatus.WAIT_FOR_PICKUP);

        this.status = DeliveryStatus.CANCELED;

        return new DeliveryCanceledEvent(id.toString(), request.userId(), refundNeeded, Instant.now());
    }

    public DeliveryFailedEvent markFailed(String reason) throws DeliveryAlreadyCompletedException {
        if (this.status == DeliveryStatus.COMPLETED)
            throw new DeliveryAlreadyCompletedException(id);
        if (this.status == DeliveryStatus.CANCELED)
            throw new IllegalStateException("Delivery already canceled");
        this.status = DeliveryStatus.FAILED;
        return new DeliveryFailedEvent(id.toString(), request.userId(), reason, Instant.now());
    }

    public void assignDrone(String droneId) {
        if (this.droneId.isPresent()) {
            throw new IllegalStateException("Drone already assigned");
        }

        this.droneId = Optional.of(droneId);
    }

    public void freeDrone(String droneId) {
        if (this.droneId.isEmpty()) {
            throw new IllegalStateException("Already no Drone");
        }

        this.droneId = Optional.empty();
    }



    //-------------------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------------------
    public boolean isCompleted() { return status == DeliveryStatus.COMPLETED; }
    public boolean isStarted() { return status == DeliveryStatus.IN_PROGRESS; }
    public boolean isCanceled() { return status == DeliveryStatus.CANCELED; }
    public boolean isReadyToStart() { return droneId.isPresent(); }
    public Optional<Location> getDroneLocation() { return Optional.ofNullable(droneLocation); }

    @Override
    public DeliveryId getId() { return id; }
    public DeliveryRequest getRequest() { return request; }
    public DeliveryStatus getStatus() { return status; }
    public Optional<String> getDroneId() { return droneId; }
    public Price getPrice() { return price; }
    public RemainingDuration getRemainingDuration() { return new RemainingDuration(eta); }
    public ETA getETA() { return eta; }
}

