package delivery_service.application.saga;

import org.springframework.stereotype.Service;

@Service
public class DeliverySagaOrchestrator {

    private final DeliverySagaRepository repository;

    public DeliverySagaOrchestrator(DeliverySagaRepository repository) {
        this.repository = repository;
    }

    public DeliverySaga start(String deliveryId) {
        return repository.save(DeliverySaga.created(deliveryId, "Saga started"));
    }

    public DeliverySaga paymentRequested(String deliveryId) {
        return transition(deliveryId, DeliverySagaState.PAYMENT_REQUESTED, "Payment requested");
    }

    public DeliverySaga paymentConfirmed(String deliveryId) {
        return transition(deliveryId, DeliverySagaState.PAYMENT_CONFIRMED, "Payment confirmed");
    }

    public DeliverySaga droneAssignmentRequested(String deliveryId) {
        return transition(deliveryId, DeliverySagaState.DRONE_ASSIGNMENT_REQUESTED, "Drone assignment requested");
    }

    public DeliverySaga waitingForPickup(String deliveryId) {
        return transition(deliveryId, DeliverySagaState.WAITING_FOR_PICKUP, "Drone assigned, waiting for pickup");
    }

    public DeliverySaga inProgress(String deliveryId) {
        return transition(deliveryId, DeliverySagaState.IN_PROGRESS, "Delivery in progress");
    }

    public DeliverySaga completed(String deliveryId) {
        return transition(deliveryId, DeliverySagaState.COMPLETED, "Delivery completed");
    }

    public DeliverySaga compensating(String deliveryId, String message) {
        return transition(deliveryId, DeliverySagaState.COMPENSATING, message);
    }

    public DeliverySaga canceled(String deliveryId) {
        return transition(deliveryId, DeliverySagaState.CANCELED, "Delivery canceled");
    }

    public DeliverySaga failed(String deliveryId, String message) {
        return transition(deliveryId, DeliverySagaState.FAILED, message);
    }

    public DeliverySaga getSaga(String deliveryId) {
        return repository.findByDeliveryId(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Saga not found for delivery " + deliveryId));
    }

    private DeliverySaga transition(String deliveryId, DeliverySagaState nextState, String message) {
        DeliverySaga current = repository.findByDeliveryId(deliveryId)
                .orElseGet(() -> DeliverySaga.created(deliveryId, "Saga auto-created"));
        return repository.save(current.transitionTo(nextState, message));
    }
}
