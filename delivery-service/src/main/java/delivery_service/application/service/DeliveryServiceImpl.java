package delivery_service.application.service;

import delivery_service.application.dto.DeliveryRequestDTO;
import delivery_service.application.exception.*;
import delivery_service.application.port.*;
import delivery_service.application.saga.DeliverySagaOrchestrator;
import delivery_service.domain.event.*;
import delivery_service.domain.model.*;
import delivery_service.domain.service.DeliveryCalculator;

import delivery_service.infrastructure.mapper.DeliveryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository repository;
    private final DeliveryCalculator calculator;
    private final PaymentPort paymentPort;
    private final DroneFleetPort droneFleetPort;
    private final DomainEventPublisher domainEventPublisher;
    private final DeliverySagaOrchestrator sagaOrchestrator;

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceImpl.class);

    public DeliveryServiceImpl(
            DeliveryRepository repository,
            DeliveryCalculator calculator,
            PaymentPort paymentPort,
            DroneFleetPort droneFleetPort,
            DomainEventPublisher domainEventPublisher,
            DeliverySagaOrchestrator sagaOrchestrator
    ) {
        this.repository = repository;
        this.calculator = calculator;
        this.paymentPort = paymentPort;
        this.droneFleetPort = droneFleetPort;
        this.domainEventPublisher = domainEventPublisher;
        this.sagaOrchestrator = sagaOrchestrator;
    }

    // -------------------------------------------------------------------------
    // CREATE DELIVERY
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public Delivery createDelivery(DeliveryRequestDTO dto) throws InvalidDeliveryRequestException {
        log.info("[DELIVERY][CREATE] Creating new delivery");

        DeliveryRequest r = DeliveryMapper.toRequest(dto);
        log.info("[DELIVERY][CREATE] request created of user id={}", r.userId());

        Distance distance = calculator.computeDistance(r.pickupLocation(), r.dropoffLocation());
        log.info("[DELIVERY][CREATE] Distance calculated ={}", distance);

        Price price = calculator.computePrice(distance, r.weight());
        log.info("[DELIVERY][CREATE] Price calculated ={}",price);

        ETA eta = DeliveryCalculator.computeETA(distance);
        log.info("[DELIVERY][CREATE] ETA calculated ={}", eta);

        Delivery delivery = new  Delivery(DeliveryId.of(repository.nextId()),r,eta,price);
        log.info("[DELIVERY][CREATE] Created new delivery with id={}", delivery.getId().id());

        repository.saveDelivery(delivery);
        log.info("[DELIVERY][CREATE] delivery saved with id={}", delivery.getId().id());
        sagaOrchestrator.start(delivery.getId().id());

        DeliveryCreatedEvent event = new DeliveryCreatedEvent(delivery.getId().toString(),delivery.getRequest().userId(), Instant.now());
        domainEventPublisher.publishEvent(event);

        return delivery;
    }

    // -------------------------------------------------------------------------
    // REQUEST PAYMENT
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public Delivery requestDeliveryStart(DeliveryId deliveryId) throws PaymentFailedException, DeliveryNotFoundException {
        log.info("[DELIVERY][REQUEST-START] Requesting with id={}", deliveryId.id());

        Delivery delivery = getDelivery(deliveryId);
        log.info("[DELIVERY][REQUEST-START] delivery found id={}", deliveryId.id());

        delivery.markPaymentPending();
        log.info("[DELIVERY][REQUEST-START] marked payment pending id={}", deliveryId.id());

        repository.updateDelivery(delivery);
        log.info("[DELIVERY][REQUEST-START] repo updated id={}", deliveryId.id());
        sagaOrchestrator.paymentRequested(deliveryId.id());

        paymentPort.requestPayment(
                delivery.getRequest().userId(),
                delivery.getId(),
                delivery.getPrice()
        );
        return delivery;
    }

    // -------------------------------------------------------------------------
    // PAYMENT ACCEPTED
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public void onPaymentAccepted(DeliveryId id)
            throws DeliveryNotFoundException, DroneAssignmentFailedException {
        log.info("[DELIVERY][ON PAYMENT ACCEPTED] on payment accepted id={}", id.id());

        Delivery d = getDelivery(id);
        log.info("[DELIVERY][ON PAYMENT ACCEPTED] delivery found id={}", id.id());

        d.markPaymentConfirmed();
        log.info("[DELIVERY][ON PAYMENT ACCEPTED] state updated id={}", id.id());

        repository.updateDelivery(d);
        log.info("[DELIVERY][ON PAYMENT ACCEPTED] repo updated id={}", id.id());
        sagaOrchestrator.paymentConfirmed(id.id());

        d.requestDrone();
        log.info("[DELIVERY][ON PAYMENT ACCEPTED] drone requested id={}", id.id());
        sagaOrchestrator.droneAssignmentRequested(id.id());

        String droneId = droneFleetPort.requestDrone(d);
        log.info("[DELIVERY][ON PAYMENT ACCEPTED] id du drone found id={} and droneId={}", id.id(), droneId);

    }

    // -------------------------------------------------------------------------
    // DRONE ASSIGNED
    // -------------------------------------------------------------------------
    @Transactional
    @Override
    public void retryDroneAssignment(DeliveryId id) throws DeliveryNotFoundException, DroneAssignmentFailedException {
        Delivery d = getDelivery(id);

        if (d.getDroneId().isPresent()) {
            return;
        }

        if (d.getStatus() != DeliveryStatus.DRONE_ASSIGNMENT_REQUESTED) {
            return;
        }

        log.info("[DELIVERY][RETRY] retry drone assignment for {}", id.id());
        sagaOrchestrator.droneAssignmentRequested(id.id());
        String droneId = droneFleetPort.requestDrone(d);
        log.info("[DELIVERY][RETRY] id du drone found id={} and droneId={}", id.id(), droneId);
    }

    // -------------------------------------------------------------------------
    // DRONE ASSIGNED
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public void onDroneAssigned(DeliveryId id, String droneId)
            throws DeliveryNotFoundException {

        Delivery d = getDelivery(id);
        d.assignDrone(droneId);
        log.info("[DELIVERY][ON DRONE ASSIGNED] delivery found id={}", id.id());
        WaitForPickUpEvent event = d.markWaitForPickUp();
        log.info("[DELIVERY][ON DRONE ASSIGNED] drone assigned id={}", id.id());
        repository.updateDelivery(d);
        log.info("[DELIVERY][ON DRONE ASSIGNED] repo updated id={}", id.id());
        sagaOrchestrator.waitingForPickup(id.id());
        domainEventPublisher.publishEvent(event);
        log.info("[DELIVERY][ON DRONE ASSIGNED] wait for pickup event published id={}", id.id());
    }

    // -------------------------------------------------------------------------
    // CANCEL DELIVERY
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public void cancelDelivery(DeliveryId id)
            throws DeliveryNotFoundException,
            DeliveryAlreadyCompletedException,
            DeliveryAlreadyStartedException,
            RefundFailedException {
        log.info("[DELIVERY][CANCEL] canceling delivery id={}", id.id());
        Delivery d = getDelivery(id);
        log.info("[DELIVERY][CANCEL] delivery found id={}", id.id());
        DeliveryCanceledEvent event = d.cancel();
        log.info("[DELIVERY][CANCEL] delivery canceled + event created id={}", id.id());
        repository.updateDelivery(d);
        log.info("[DELIVERY][CANCEL] repo updated id={}", id.id());
        if (event.refundNeeded()) {
            sagaOrchestrator.compensating(id.id(), "Refund requested during cancellation");
            paymentPort.requestRefund(d.getRequest().userId(), id, d.getPrice());
            log.info("[DELIVERY][CANCEL] refund requested id={}", id.id());
        }

        domainEventPublisher.publishEvent(event);
        log.info("[DELIVERY][CANCEL] cancel-event published id={}", id.id());
        sagaOrchestrator.canceled(id.id());
        d.freeDrone(d.getDroneId().toString());
    }


    // -------------------------------------------------------------------------
    // FAILURE
    // -------------------------------------------------------------------------
    @Override
    @Transactional
    public void failDelivery(DeliveryId id, String reason)
            throws DeliveryNotFoundException, DeliveryAlreadyCompletedException, RefundFailedException {
        log.info("[DELIVERY][FAIL] failing delivery id={}", id.id());
        Delivery d = getDelivery(id);
        log.info("[DELIVERY][FAIL] delivery found id={}", id.id());
        DeliveryFailedEvent event = d.markFailed(reason);
        log.info("[DELIVERY][FAIL] delivery failed + event created id={}", id.id());
        repository.updateDelivery(d);
        log.info("[DELIVERY][FAIL] repo updated id={}", id.id());
        sagaOrchestrator.compensating(id.id(), "Compensation started after failure: " + reason);
        paymentPort.requestRefund(d.getRequest().userId(), id, d.getPrice());
        log.info("[DELIVERY][FAIL] refund requested id={}", id.id());
        domainEventPublisher.publishEvent(event);
        log.info("[DELIVERY][FAIL] event published id={}", id.id());
        sagaOrchestrator.failed(id.id(), reason);
        d.freeDrone(d.getDroneId().toString());
    }


    @Transactional
    @Override
    public void onDroneAtPickup(DeliveryId deliveryId) throws DeliveryNotFoundException {
        log.info("[DELIVERY][PICKUP] At pickup for delivery with id={}", deliveryId.id());
        Delivery delivery = getDelivery(deliveryId);
        log.info("[DELIVERY][PICKUP] At pickup for delivery with id={}", deliveryId.id());
        DeliveryStartedEvent event = delivery.markPickedUp();
        log.info("[DELIVERY][PICKUP] order picked-up with id={}", deliveryId.id());
        repository.updateDelivery(delivery);
        log.info("[DELIVERY][PICKUP] repo updated id={}", deliveryId.id());
        sagaOrchestrator.inProgress(deliveryId.id());
        domainEventPublisher.publishEvent(event);
        log.info("[DELIVERY][PICKUP] drone-at-picked event published id={}", deliveryId.id());
    }

    @Transactional
    @Override
    public void onDroneDelivered(DeliveryId deliveryId) throws DeliveryNotFoundException {
        log.info("[DELIVERY][DELIVERED] on drone delivered for delivery with id={}", deliveryId.id());
        Delivery delivery = getDelivery(deliveryId);
        log.info("[DELIVERY][DELIVERED] delivery found id={}", deliveryId.id());
        DeliveryCompletedEvent event = delivery.markDelivered();
        log.info("[DELIVERY][DELIVERED] delivery delivered id={}", deliveryId.id());
        repository.saveDelivery(delivery);
        log.info("[DELIVERY][DELIVERED] repo updated id={}", deliveryId.id());
        sagaOrchestrator.completed(deliveryId.id());
        domainEventPublisher.publishEvent(event);
        log.info("[DELIVERY][DELIVERED] delivered event published id={}", deliveryId.id());
        delivery.freeDrone(delivery.getDroneId().toString());
    }

    @Transactional
    @Override
    public void onDroneLocationUpdated(DeliveryId deliveryId, String droneId, double latitude, double longitude) throws DeliveryNotFoundException {
        log.info("[DELIVERY][LOCATION UPDATED] delivered event published id={} and droneId = {}", deliveryId.id(), droneId);
        Delivery delivery = getDelivery(deliveryId);
        log.info("[DELIVERY][LOCATION UPDATED] delivered found id={}", deliveryId.id());
        EtaUpdatedEvent event = delivery.updateDroneLocation(new Location(latitude, longitude));
        log.info("[DELIVERY][LOCATION UPDATED] drone ETA updated + event created id={} and droneId = {} with latitude = {} and longitude = {}", deliveryId.id(), droneId, latitude, longitude);

        repository.saveDelivery(delivery);
        log.info("[DELIVERY][LOCATION UPDATED] repo updated id={}", deliveryId.id());
        domainEventPublisher.publishEvent(event);
        log.info("[DELIVERY][LOCATION UPDATED] ETA updated event published id={} and droneId = {} with latitude = {} and longitude = {}", deliveryId.id(), droneId, latitude, longitude);
    }

    public Delivery getDelivery(DeliveryId deliveryId) throws DeliveryNotFoundException {
        return repository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryStatus getDeliveryStatus(DeliveryId id) throws DeliveryNotFoundException {
        return repository.findById(id)
                .orElseThrow(() -> new DeliveryNotFoundException(id))
                .getStatus();
    }

    @Override
    @Transactional(readOnly = true)
    public RemainingDuration getTimeLeft(DeliveryId id) throws DeliveryNotFoundException {
        Delivery d = repository.findById(id)
                .orElseThrow(() -> new DeliveryNotFoundException(id));

        return d.getRemainingDuration();
    }

    @Override
    public boolean deliveryExists(DeliveryId id) {
        return repository.existsById(id);
    }

    @Override
    public Location getPickupLocation(DeliveryId id) throws DeliveryNotFoundException {
        Delivery d = getDelivery(id);
        return d.getRequest().pickupLocation();
    }

    @Override
    public Location getDropoffLocation(DeliveryId id) throws DeliveryNotFoundException {
        Delivery d = getDelivery(id);
        return d.getRequest().dropoffLocation();
    }

    @Override
    public Optional<Location> getDroneLocation(DeliveryId id) throws DeliveryNotFoundException {
        Delivery d = getDelivery(id);
        return d.getDroneLocation();
    }


}
